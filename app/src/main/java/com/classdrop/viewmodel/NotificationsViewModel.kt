package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.Notification
import com.classdrop.model.NotificationDto
import com.classdrop.model.NotificationType
import com.classdrop.network.NetworkResult
import com.classdrop.repository.NotificationsApiRepository
import com.classdrop.utils.TimeUtils
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificationsApiRepository(application)

    private val _notificationsState = MutableLiveData<NetworkResult<List<Notification>>>()
    val notificationsState: LiveData<NetworkResult<List<Notification>>> = _notificationsState

    private val _unreadCount = MutableLiveData(0)
    val unreadCount: LiveData<Int> = _unreadCount

    fun fetchNotifications() {
        _notificationsState.value = NetworkResult.Loading()
        viewModelScope.launch {
            when (val result = repository.obtenerNotificaciones()) {
                is NetworkResult.Success -> {
                    val notificaciones = result.data?.map { it.aUiModel() } ?: emptyList()
                    _notificationsState.value = NetworkResult.Success(notificaciones)
                    _unreadCount.value = notificaciones.count { !it.isRead }
                }
                is NetworkResult.Error -> {
                    _notificationsState.value =
                        NetworkResult.Error(result.message ?: "No se pudieron cargar las notificaciones")
                }
                else -> {}
            }
        }
    }

    fun fetchUnreadCount() {
        viewModelScope.launch {
            val result = repository.obtenerContadorNoLeidas()
            if (result is NetworkResult.Success) {
                _unreadCount.value = result.data ?: 0
            }
        }
    }

    // Marca UNA notificación como leída. Actualiza la lista local al instante
    // (el punto de "no leída" desaparece sin esperar al servidor) y de todos
    // modos llama al API para que quede guardado.
    fun markAsRead(notificationId: String) {
        val listaActual = (_notificationsState.value as? NetworkResult.Success)?.data ?: return
        val yaEstabaLeida = listaActual.firstOrNull { it.id == notificationId }?.isRead == true
        if (yaEstabaLeida) return

        val listaActualizada = listaActual.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        _notificationsState.value = NetworkResult.Success(listaActualizada)
        _unreadCount.value = listaActualizada.count { !it.isRead }

        viewModelScope.launch {
            repository.marcarLeida(notificationId)
        }
    }

    fun markAllAsRead() {
        val listaActual = (_notificationsState.value as? NetworkResult.Success)?.data ?: return
        val listaActualizada = listaActual.map { it.copy(isRead = true) }
        _notificationsState.value = NetworkResult.Success(listaActualizada)
        _unreadCount.value = 0

        viewModelScope.launch {
            repository.marcarTodasLeidas()
        }
    }

    private fun NotificationDto.aUiModel(): Notification {
        val tipoNotificacion = when (tipo.lowercase()) {
            "exito" -> NotificationType.SUCCESS
            "error" -> NotificationType.ERROR
            "advertencia" -> NotificationType.WARNING
            else -> NotificationType.INFO
        }
        return Notification(
            id = id,
            title = titulo,
            message = cuerpo,
            time = TimeUtils.tiempoRelativo(creadoEn),
            isRead = leida,
            type = tipoNotificacion,
            archivoId = archivoId
        )
    }
}