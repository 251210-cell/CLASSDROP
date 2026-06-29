package com.classdrop.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.databinding.ItemReportCommentBinding
import com.classdrop.model.CommentReport

class ReportsAdapter(
    private val onKeep: (CommentReport) -> Unit,
    private val onRemove: (CommentReport) -> Unit
) : ListAdapter<CommentReport, ReportsAdapter.ViewHolder>(ReportDiffCallback()) {

    inner class ViewHolder(val binding: ItemReportCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = getItem(position)
        holder.binding.apply {
            tvReporterAvatar.text = report.reporterName.split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")
            tvReporterName.text = "Reportado por ${report.reporterName}"
            tvReportTime.text = report.time
            tvDislikeCount.text = report.dislikes
            tvReportContext.text = report.contextTitle
            tvReportedUser.text = "Comentario Reportado (Usuario: ${report.reportedUserName}):"
            tvCommentContent.text = report.commentContent

            btnKeep.setOnClickListener { onKeep(report) }
            btnRemove.setOnClickListener { onRemove(report) }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<CommentReport>() {
        override fun areItemsTheSame(oldItem: CommentReport, newItem: CommentReport): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CommentReport, newItem: CommentReport): Boolean = oldItem == newItem
    }
}
