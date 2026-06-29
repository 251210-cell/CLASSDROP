package com.classdrop.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.classdrop.model.Notification
import com.classdrop.model.NotificationType

object NotificationRepository {
    private val _notifications = MutableLiveData<List<Notification>>(mutableListOf())
    val notifications: LiveData<List<Notification>> = _notifications

    init {
        // Notificaciones de ejemplo para el usuario
        val initialList = mutableListOf(
            Notification(
                id = "1",
                title = "¡Archivo Publicado!",
                message = "Tu apunte 'Cálculo III - Derivadas' ha pasado la revisión y ya está disponible.",
                time = "Hace 5 min",
                type = NotificationType.SUCCESS
            ),
            Notification(
                id = "2",
                title = "Archivo Rechazado",
                message = "Tu documento no pudo ser publicado por incumplir las normas de integridad.",
                time = "Hace 1 hora",
                type = NotificationType.ERROR
            ),
            Notification(
                id = "3",
                title = "Nuevo Comentario",
                message = "Alguien ha comentado en tu publicación de Física II.",
                time = "Ayer",
                type = NotificationType.INFO,
                isRead = true
            )
        )
        _notifications.value = initialList
    }

    fun addNotification(title: String, message: String, type: NotificationType) {
        val newNotification = Notification(
            id = System.currentTimeMillis().toString(),
            title = title,
            message = message,
            time = "Ahora",
            type = type
        )
        val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, newNotification)
        _notifications.value = currentList
    }
}
