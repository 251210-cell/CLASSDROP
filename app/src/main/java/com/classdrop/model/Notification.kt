package com.classdrop.model

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.INFO,
    // Si la notificación se refiere a un archivo, permite navegar directo a
    // su detalle en vez de adivinar el destino por el texto del título.
    val archivoId: String? = null
)

enum class NotificationType {
    INFO,
    WARNING,
    SUCCESS,
    ERROR
}