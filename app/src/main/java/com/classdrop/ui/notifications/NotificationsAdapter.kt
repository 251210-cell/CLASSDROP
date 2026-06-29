package com.classdrop.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.classdrop.R
import com.classdrop.databinding.ItemNotificationBinding
import com.classdrop.model.Notification
import com.classdrop.model.NotificationType

class NotificationsAdapter(
    private val onNotificationClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationsAdapter.ViewHolder>(NotificationDiffCallback()) {

    inner class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onNotificationClick(getItem(position))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position)
        holder.binding.apply {
            tvNotificationTitle.text = notification.title
            tvNotificationMessage.text = notification.message
            tvNotificationTime.text = notification.time
            viewUnreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            val context = holder.itemView.context
            val (icon, color) = when (notification.type) {
                NotificationType.SUCCESS -> Pair(R.drawable.ic_check_circle, R.color.file_teal_text)
                NotificationType.ERROR -> Pair(R.drawable.ic_warning, R.color.error)
                NotificationType.WARNING -> Pair(R.drawable.ic_warning, R.color.file_pink_text)
                NotificationType.INFO -> Pair(R.drawable.ic_notification, R.color.primary)
            }

            ivNotificationIcon.setImageResource(icon)
            ivNotificationIcon.setColorFilter(ContextCompat.getColor(context, color))
            ivIconContainer.setCardBackgroundColor(ContextCompat.getColor(context, color).let { 
                android.graphics.Color.argb(30, android.graphics.Color.red(it), android.graphics.Color.green(it), android.graphics.Color.blue(it))
            })
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean = oldItem == newItem
    }
}
