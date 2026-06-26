package com.classdrop.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemPostBinding
import androidx.core.content.ContextCompat
import com.classdrop.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class Post(
    val id: String,
    val userName: String,
    val time: String,
    val fileName: String,
    val fileType: String,
    val fileSize: String = "1.2 MB",
    var likes: Int,
    var dislikes: Int = 0,
    val downloads: Int,
    val comments: Int,
    var isLiked: Boolean = false,
    var isDisliked: Boolean = false,
    var isBookmarked: Boolean = false,
    var isDownloaded: Boolean = false
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
            tvDislikes.text = post.dislikes.toString()
            tvDownloads.text = post.downloads.toString()
            tvComments.text = post.comments.toString()

            // Configurar icono y color dinámico
            setupFileTypeIcon(holder, post.fileType)

            // Lógica de Like
            updateLikeUI(holder, post.isLiked)
            llLike.setOnClickListener {
                post.isLiked = !post.isLiked
                if (post.isLiked) {
                    post.likes++
                    if (post.isDisliked) {
                        post.isDisliked = false
                        post.dislikes--
                        updateDislikeUI(holder, false, post.dislikes)
                    }
                } else {
                    post.likes--
                }
                
                tvLikes.text = post.likes.toString()
                updateLikeUI(holder, post.isLiked)
                animateButton(ivLikeIcon)
            }

            // Lógica de Dislike
            updateDislikeUI(holder, post.isDisliked, post.dislikes)
            llDislike.setOnClickListener {
                post.isDisliked = !post.isDisliked
                if (post.isDisliked) {
                    post.dislikes++
                    if (post.isLiked) {
                        post.isLiked = false
                        post.likes--
                        tvLikes.text = post.likes.toString()
                        updateLikeUI(holder, false)
                    }
                } else {
                    post.dislikes--
                }
                updateDislikeUI(holder, post.isDisliked, post.dislikes)
                animateButton(ivDislikeIcon)
            }

            // Lógica de Favoritos (Bookmark) en ROJO
            updateBookmarkUI(holder, post.isBookmarked)
            btnBookmark.setOnClickListener {
                post.isBookmarked = !post.isBookmarked
                updateBookmarkUI(holder, post.isBookmarked)
                animateButton(btnBookmark)
            }

            // Lógica de Descarga con Alerta y cambio de color
            updateDownloadUI(holder, post.isDownloaded)
            llDownload.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Descargar archivo")
                    .setMessage("¿Deseas descargar '${post.fileName}' en tu dispositivo?")
                    .setPositiveButton("Descargar") { _, _ ->
                        post.isDownloaded = true
                        updateDownloadUI(holder, true)
                        animateButton(ivDownloadIcon)
                        android.widget.Toast.makeText(holder.itemView.context, "Iniciando descarga...", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            // Lógica de Comentarios
            llComments.setOnClickListener {
                animateButton(ivCommentIcon)
                val intent = android.content.Intent(holder.itemView.context, com.classdrop.ui.files.FileDetailActivity::class.java).apply {
                    putExtra("FILE_NAME", post.fileName)
                    putExtra("FILE_TYPE", post.fileType)
                    putExtra("FILE_SIZE", post.fileSize)
                }
                holder.itemView.context.startActivity(intent)
            }

            // Click en la tarjeta principal también abre el detalle
            root.setOnClickListener {
                val intent = android.content.Intent(holder.itemView.context, com.classdrop.ui.files.FileDetailActivity::class.java).apply {
                    putExtra("FILE_NAME", post.fileName)
                    putExtra("FILE_TYPE", post.fileType)
                    putExtra("FILE_SIZE", post.fileSize)
                }
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    private fun animateButton(view: android.view.View) {
        view.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun updateLikeUI(holder: PostViewHolder, isLiked: Boolean) {
        val context = holder.itemView.context
        // Usamos el mismo morado del Dislike para el Like activo
        val color = if (isLiked) {
            android.graphics.Color.parseColor("#A855F7")
        } else {
            ContextCompat.getColor(context, R.color.placeholder)
        }
        
        holder.binding.ivLikeIcon.setColorFilter(color)
        holder.binding.tvLikes.setTextColor(color)
    }

    private fun updateDislikeUI(holder: PostViewHolder, isDisliked: Boolean, count: Int) {
        val context = holder.itemView.context
        // Color morado específico del Home para Dislike activo (#A855F7)
        val color = if (isDisliked) {
            android.graphics.Color.parseColor("#A855F7")
        } else {
            ContextCompat.getColor(context, R.color.placeholder)
        }
        holder.binding.ivDislikeIcon.setColorFilter(color)
        holder.binding.tvDislikes.apply {
            text = count.toString()
            setTextColor(color)
        }
    }

    private fun updateBookmarkUI(holder: PostViewHolder, isBookmarked: Boolean) {
        val context = holder.itemView.context
        if (isBookmarked) {
            // Rojo brillante como en el Home
            holder.binding.btnBookmark.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_light))
        } else {
            holder.binding.btnBookmark.setColorFilter(ContextCompat.getColor(context, R.color.placeholder))
        }
    }

    private fun updateDownloadUI(holder: PostViewHolder, isDownloaded: Boolean) {
        val context = holder.itemView.context
        val color = if (isDownloaded) {
            android.graphics.Color.parseColor("#A855F7")
        } else {
            ContextCompat.getColor(context, R.color.placeholder)
        }
        holder.binding.ivDownloadIcon.setColorFilter(color)
        holder.binding.tvDownloads.setTextColor(color)
    }

    private fun setupFileTypeIcon(holder: PostViewHolder, fileType: String) {
        val context = holder.itemView.context
        val (iconRes, bgColor, textColor) = when (fileType.uppercase()) {
            "PDF" -> Triple(
                R.drawable.ic_mortarboard,
                ContextCompat.getColor(context, R.color.file_pdf_bg),
                ContextCompat.getColor(context, R.color.file_pdf_text)
            )
            "DOCX", "DOC" -> Triple(
                R.drawable.ic_file_doc,
                ContextCompat.getColor(context, R.color.file_pink_bg),
                ContextCompat.getColor(context, R.color.file_pink_text)
            )
            "JPG", "PNG", "IMG" -> Triple(
                R.drawable.ic_image,
                ContextCompat.getColor(context, R.color.file_teal_bg),
                ContextCompat.getColor(context, R.color.file_teal_text)
            )
            else -> Triple(
                R.drawable.ic_file_doc,
                ContextCompat.getColor(context, R.color.surface_variant),
                ContextCompat.getColor(context, R.color.text_secondary)
            )
        }

        holder.binding.ivFileTypeIcon.apply {
            setImageResource(iconRes)
            setBackgroundResource(R.drawable.bg_rounded_square_primary)
            backgroundTintList = android.content.res.ColorStateList.valueOf(bgColor)
            setColorFilter(textColor)
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}