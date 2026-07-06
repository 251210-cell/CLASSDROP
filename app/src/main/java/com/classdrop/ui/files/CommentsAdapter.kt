package com.classdrop.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemCommentBinding
import com.classdrop.model.Comment
import com.classdrop.utils.TimeUtils

class CommentsAdapter(
    private val onDeleteClick: (String) -> Unit,
    private val onLikeChanged: ((Comment) -> Unit)? = null,
    private val onDislikeChanged: ((Comment) -> Unit)? = null
) : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding, onDeleteClick, onLikeChanged, onDislikeChanged)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding,
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

                // IMPORTANTE: totalLikes, totalDislikes, isLiked e isDisliked ahora
                // vienen calculados en vivo desde el backend (COUNT(*) real sobre
                // likes_comentarios / dislikes_comentarios), igual que ya pasa con
                // los archivos. Ya NO hacemos matemática local (comment.totalLikes++/--)
                // ni dependemos de SessionManager para saber si el usuario ya
                // reaccionó: eso es justo lo que causaba que el contador se
                // desincronizara y llegara a mostrar números negativos.
                updateReactionsUI(comment)

                btnLike.setOnClickListener {
                    // Optimista SOLO para que el ícono responda al instante; el
                    // número real y definitivo llega en el próximo fetchComments(),
                    // que se dispara justo después de esta llamada.
                    val nuevoEstado = !comment.isLiked
                    onLikeChanged?.invoke(comment.copy(isLiked = nuevoEstado))
                }

                btnDislike.setOnClickListener {
                    val nuevoEstado = !comment.isDisliked
                    onDislikeChanged?.invoke(comment.copy(isDisliked = nuevoEstado))
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