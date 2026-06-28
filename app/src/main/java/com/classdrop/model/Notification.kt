package com.classdrop.model

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.INFO
)

enum class NotificationType {
    INFO,
    WARNING,
    SUCCESS,
    ERROR
}
