package com.classdrop.ui.admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemModerationBinding
import com.classdrop.model.ModerationTask
import com.classdrop.ui.files.FilePreviewActivity

class ModerationAdapter(
    private val onApprove: (ModerationTask) -> Unit,
    private val onReject: (ModerationTask) -> Unit
) : ListAdapter<ModerationTask, ModerationAdapter.ViewHolder>(ModerationDiffCallback()) {

    inner class ViewHolder(val binding: ItemModerationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemModerationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        holder.binding.apply {
            tvFileName.text = task.fileName
            tvUploaderInfo.text = "Subido por: ${task.userName} • ${task.time}"
            tvFlagReason.text = task.flagReason

            // Permitir al admin previsualizar el archivo antes de decidir
            root.setOnClickListener {
                if (!task.fileUrl.isNullOrEmpty()) {
                    val intent = Intent(holder.itemView.context, FilePreviewActivity::class.java).apply {
                        putExtra("FILE_URL", task.fileUrl)
                        putExtra("FILE_NAME", task.fileName)
                        putExtra("FILE_TYPE", task.fileType)
                    }
                    holder.itemView.context.startActivity(intent)
                }
            }

            btnApprove.setOnClickListener { onApprove(task) }
            btnReject.setOnClickListener { onReject(task) }
        }
    }

    class ModerationDiffCallback : DiffUtil.ItemCallback<ModerationTask>() {
        override fun areItemsTheSame(oldItem: ModerationTask, newItem: ModerationTask): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ModerationTask, newItem: ModerationTask): Boolean = oldItem == newItem
    }
}
