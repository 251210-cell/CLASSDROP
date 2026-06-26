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
                tvCommentUserName.text = "Usuario ${comment.userId.takeLast(4)}" // Mock user name
                tvCommentContent.text = comment.content
                
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvCommentTime.text = sdf.format(Date(comment.timestamp))

                // Asignar un color aleatorio o fijo al avatar si es necesario
                cvCommentAvatar.setCardBackgroundColor(android.graphics.Color.parseColor("#6366F1"))
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem == newItem
    }
}
