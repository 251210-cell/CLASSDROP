package com.classdrop.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.classdrop.model.Notification
import com.classdrop.model.NotificationType

object NotificationRepository {
    private val _notifications = MutableLiveData<List<Notification>>(mutableListOf())
    val notifications: LiveData<List<Notification>> = _notifications

    // Cuando conecten el backend, esta lista se poblará con datos reales
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

    fun setNotifications(newList: List<Notification>) {
        _notifications.value = newList
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}
