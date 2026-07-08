package com.classdrop.ui.files

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.classdrop.R
import com.classdrop.databinding.ItemUploadFileBinding
import com.classdrop.databinding.ItemUploadUrlBinding

class UploadPagerAdapter(
    private val onFileClick: () -> Unit,
    private val onUrlChanged: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedFileName: String? = null
    private var selectedFileUri: Uri? = null

    fun setSelectedFile(name: String, uri: Uri?) {
        selectedFileName = name
        selectedFileUri = uri
        notifyItemChanged(0)
    }

    companion object {
        private const val TYPE_FILE = 0
        private const val TYPE_URL = 1
    }

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_FILE else TYPE_URL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_FILE) {
            FileViewHolder(ItemUploadFileBinding.inflate(inflater, parent, false))
        } else {
            UrlViewHolder(ItemUploadUrlBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FileViewHolder) {
            holder.binding.uploadAreaFile.setOnClickListener { onFileClick() }
            
            if (selectedFileName != null) {
                holder.binding.tvSelectedFileName.text = selectedFileName
                holder.binding.tvSelectedFileName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.primary))
                holder.binding.tvChangeFile.visibility = View.VISIBLE
                
                val context = holder.itemView.context
                val mimeType = selectedFileUri?.let { context.contentResolver.getType(it) } ?: ""

                if (mimeType.startsWith("image/")) {
                    holder.binding.ivFilePreview.visibility = View.VISIBLE
                    holder.binding.ivFileIcon.visibility = View.GONE
                    Glide.with(context)
                        .load(selectedFileUri)
                        .centerCrop()
                        .into(holder.binding.ivFilePreview)
                } else {
                    holder.binding.ivFilePreview.visibility = View.GONE
                    holder.binding.ivFileIcon.visibility = View.VISIBLE
                    
                    // Si es un documento o PDF, mostramos el icono de archivo en lugar del check
                    val iconRes = if (mimeType.contains("pdf") || mimeType.contains("word") || mimeType.contains("officedocument")) {
                        R.drawable.ic_file_doc
                    } else {
                        R.drawable.ic_file_doc // Genérico para otros archivos
                    }
                    
                    holder.binding.ivFileIcon.setImageResource(iconRes)
                    holder.binding.ivFileIcon.layoutParams.width = (64 * context.resources.displayMetrics.density).toInt()
                    holder.binding.ivFileIcon.layoutParams.height = (64 * context.resources.displayMetrics.density).toInt()
                    holder.binding.ivFileIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
                }
            } else {
                holder.binding.tvSelectedFileName.text = "Toca para seleccionar archivo"
                holder.binding.tvSelectedFileName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
                holder.binding.ivFileIcon.visibility = View.VISIBLE
                holder.binding.ivFileIcon.setImageResource(R.drawable.ic_nav_upload)
                holder.binding.ivFileIcon.layoutParams.width = (48 * holder.itemView.context.resources.displayMetrics.density).toInt()
                holder.binding.ivFileIcon.layoutParams.height = (48 * holder.itemView.context.resources.displayMetrics.density).toInt()
                holder.binding.ivFileIcon.clearColorFilter()
                holder.binding.ivFilePreview.visibility = View.GONE
                holder.binding.tvChangeFile.visibility = View.GONE
            }
        } else if (holder is UrlViewHolder) {
            holder.binding.etUrl.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    onUrlChanged(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun getItemCount(): Int = 2

    class FileViewHolder(val binding: ItemUploadFileBinding) : RecyclerView.ViewHolder(binding.root)
    class UrlViewHolder(val binding: ItemUploadUrlBinding) : RecyclerView.ViewHolder(binding.root)
}
