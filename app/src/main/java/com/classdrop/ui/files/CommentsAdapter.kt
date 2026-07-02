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

class CommentsAdapter(
    // Recibimos la lambda para poder gestionar la eliminación del comentario
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.apply {
                // 1. Usamos userId de tu modelo para pintar un nombre identificador
                val userName = "Usuario ${comment.userId.takeLast(4)}"
                tvCommentUserName.text = userName
                tvCommentContent.text = comment.content

                // Generar iniciales del avatar
                tvCommentAvatar.text = userName.split(" ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .take(2)
                    .joinToString("")

                // 2. Mantenemos tu formateo de hora usando el timestamp (Long) de tu modelo
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvCommentTime.text = sdf.format(Date(comment.timestamp))

                // 3. Inicializamos y actualizamos la interfaz de Likes/Dislikes con tus campos correspondientes
                updateLikesUI(comment)

                btnLike.setOnClickListener {
                    toggleLike(comment)
                    updateLikesUI(comment)
                }

                btnDislike.setOnClickListener {
                    toggleDislike(comment)
                    updateLikesUI(comment)
                }

                // Clic largo para activar la lambda de borrado usando comment.id
                root.setOnLongClickListener {
                    onDeleteClick(comment.id)
                    true
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