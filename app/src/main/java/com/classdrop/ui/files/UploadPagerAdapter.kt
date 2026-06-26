package com.classdrop.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemUploadFileBinding
import com.classdrop.databinding.ItemUploadUrlBinding

class UploadPagerAdapter(
    private val onFileClick: () -> Unit,
    private val onUrlChanged: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        } else if (holder is UrlViewHolder) {
            // Logic for URL changes if needed
        }
    }

    override fun getItemCount(): Int = 2

    class FileViewHolder(val binding: ItemUploadFileBinding) : RecyclerView.ViewHolder(binding.root)
    class UrlViewHolder(val binding: ItemUploadUrlBinding) : RecyclerView.ViewHolder(binding.root)
}