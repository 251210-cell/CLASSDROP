package com.classdrop.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemCommentBinding
import com.classdrop.model.Comment
import com.classdrop.utils.SessionManager
import com.classdrop.utils.TimeUtils

class CommentsAdapter(
    private val sessionManager: SessionManager? = null,
    private val onDeleteClick: (String) -> Unit,
    private val onLikeChanged: ((Comment) -> Unit)? = null,
    private val onDislikeChanged: ((Comment) -> Unit)? = null
) : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding, sessionManager, onDeleteClick, onLikeChanged, onDislikeChanged)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding,
        private val sessionManager: SessionManager?,
        private val onDeleteClick: (String) -> Unit,
        private val onLikeChanged: ((Comment) -> Unit)?,
        private val onDislikeChanged: ((Comment) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.apply {
                val userName = comment.autor?.nombreCompleto ?: "Usuario"
                tvCommentUserName.text = userName
                tvCommentContent.text = comment.contenido

                tvCommentAvatar.text = userName.split(" ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .take(2)
                    .joinToString("")

                tvCommentTime.text = TimeUtils.tiempoRelativo(comment.creadoEn)

                // El backend no nos dice si YO ya di like/dislike a este comentario
                // cuando se recarga la lista (por ejemplo al enviar un comentario nuevo),
                // así que lo restauramos desde el almacenamiento local para que el botón
                // se mantenga resaltado.
                comment.isLiked = sessionManager?.isCommentLiked(comment.id) ?: comment.isLiked
                comment.isDisliked = sessionManager?.isCommentDisliked(comment.id) ?: comment.isDisliked

                updateReactionsUI(comment)

                btnLike.setOnClickListener {
                    if (comment.isLiked) {
                        comment.isLiked = false
                        comment.totalLikes--
                    } else {
                        comment.isLiked = true
                        comment.totalLikes++
                        if (comment.isDisliked) {
                            comment.isDisliked = false
                            comment.totalDislikes--
                            sessionManager?.setCommentDisliked(comment.id, false)
                        }
                    }
                    sessionManager?.setCommentLiked(comment.id, comment.isLiked)
                    updateReactionsUI(comment)
                    onLikeChanged?.invoke(comment)
                }

                btnDislike.setOnClickListener {
                    if (comment.isDisliked) {
                        comment.isDisliked = false
                        comment.totalDislikes--
                    } else {
                        comment.isDisliked = true
                        comment.totalDislikes++
                        if (comment.isLiked) {
                            comment.isLiked = false
                            comment.totalLikes--
                            sessionManager?.setCommentLiked(comment.id, false)
                        }
                    }
                    sessionManager?.setCommentDisliked(comment.id, comment.isDisliked)
                    updateReactionsUI(comment)
                    onDislikeChanged?.invoke(comment)
                }

                // Clic largo para activar la lambda de borrado usando comment.id
                root.setOnLongClickListener {
                    onDeleteClick(comment.id)
                    true
                }
            }
        }

        private fun updateReactionsUI(comment: Comment) {
            binding.apply {
                tvLikeCount.text = comment.totalLikes.toString()
                tvDislikeCount.text = comment.totalDislikes.toString()

                val activeColor = android.graphics.Color.parseColor("#6366F1")
                val inactiveColor = android.graphics.Color.parseColor("#94A3B8")

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
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem == newItem
    }
}