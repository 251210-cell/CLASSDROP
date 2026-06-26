package com.classdrop.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemPostBinding

data class Post(
    val id: String,
    val userName: String,
    val time: String,
    val fileName: String,
    val fileType: String,
    var likes: Int,
    val downloads: Int,
    val comments: Int,
    var isLiked: Boolean = false
)

class PostsAdapter : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.binding.apply {
            tvUserName.text = post.userName
            tvTime.text = post.time
            tvFileName.text = post.fileName
            tvFileTypeLabel.text = post.fileType
            tvLikes.text = post.likes.toString()
            tvDownloads.text = post.downloads.toString()
            tvComments.text = post.comments.toString()

            // Lógica de Like
            updateLikeUI(holder, post.isLiked)

            llLike.setOnClickListener {
                post.isLiked = !post.isLiked
                if (post.isLiked) post.likes++ else post.likes--
                
                tvLikes.text = post.likes.toString()
                updateLikeUI(holder, post.isLiked)

                // Animación de "pop"
                ivLikeIcon.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(100)
                    .withEndAction {
                        ivLikeIcon.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
        }
    }

    private fun updateLikeUI(holder: PostViewHolder, isLiked: Boolean) {
        val context = holder.itemView.context
        val color = if (isLiked) {
            androidx.core.content.ContextCompat.getColor(context, com.classdrop.R.color.primary)
        } else {
            androidx.core.content.ContextCompat.getColor(context, com.classdrop.R.color.text_secondary)
        }
        
        holder.binding.ivLikeIcon.setColorFilter(color)
        holder.binding.tvLikes.setTextColor(color)
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}