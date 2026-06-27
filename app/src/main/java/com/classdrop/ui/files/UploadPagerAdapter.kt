package com.classdrop.ui.files

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemUploadFileBinding
import com.classdrop.databinding.ItemUploadUrlBinding

class UploadPagerAdapter(
    private val onFileClick: () -> Unit,
    private val onUrlChanged: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedFileName: String? = null

    fun setSelectedFileName(name: String) {
        selectedFileName = name
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
            } else {
                holder.binding.tvSelectedFileName.text = "Toca para seleccionar archivo"
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
