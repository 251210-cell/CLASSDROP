package com.classdrop.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemCommentBinding
import com.classdrop.model.Comment
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.apply {
                val userName = "Usuario ${comment.userId.takeLast(4)}"
                tvCommentUserName.text = userName
                tvCommentContent.text = comment.content
                
                // Generar iniciales del usuario
                tvCommentAvatar.text = userName.split(" ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .take(2)
                    .joinToString("")

                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvCommentTime.text = sdf.format(Date(comment.timestamp))

                // Actualizar UI de Likes/Dislikes
                updateLikesUI(comment)

                btnLike.setOnClickListener {
                    toggleLike(comment)
                    updateLikesUI(comment)
                }

                btnDislike.setOnClickListener {
                    toggleDislike(comment)
                    updateLikesUI(comment)
                }
            }
        }

        private fun updateLikesUI(comment: Comment) {
            binding.apply {
                tvLikeCount.text = comment.likes.toString()
                tvDislikeCount.text = comment.dislikes.toString()

                val activeColor = android.graphics.Color.parseColor("#6366F1") // Primary
                val inactiveColor = android.graphics.Color.parseColor("#94A3B8") // Slate 400

                ivLike.imageTintList = android.content.res.ColorStateList.valueOf(
                    if (comment.isLiked) activeColor else inactiveColor
                )
                tvLikeCount.setTextColor(if (comment.isLiked) activeColor else inactiveColor)

                ivDislike.imageTintList = android.content.res.ColorStateList.valueOf(
                    if (comment.isDisliked) activeColor else inactiveColor
                )
                tvDislikeCount.setTextColor(if (comment.isDisliked) activeColor else inactiveColor)
            }
        }

        private fun toggleLike(comment: Comment) {
            if (comment.isLiked) {
                comment.isLiked = false
                comment.likes--
            } else {
                comment.isLiked = true
                comment.likes++
                if (comment.isDisliked) {
                    comment.isDisliked = false
                    comment.dislikes--
                }
            }
        }

        private fun toggleDislike(comment: Comment) {
            if (comment.isDisliked) {
                comment.isDisliked = false
                comment.dislikes--
            } else {
                comment.isDisliked = true
                comment.dislikes++
                if (comment.isLiked) {
                    comment.isLiked = false
                    comment.likes--
                }
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem == newItem
    }
}
