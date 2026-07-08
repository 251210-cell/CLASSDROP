package com.classdrop.repository

import android.content.Context
import com.classdrop.model.NotificationDto
import com.classdrop.network.NetworkResult
import com.classdrop.network.NotificationsService
import com.classdrop.network.RetrofitClient

class NotificationsApiRepository(context: Context) {

    private val notificationsService: NotificationsService =
        RetrofitClient.create(context).create(NotificationsService::class.java)

    suspend fun obtenerNotificaciones(): NetworkResult<List<NotificationDto>> {
        return try {
            val response = notificationsService.getNotificaciones(limite = 50, offset = 0)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    NetworkResult.Success(body.data.rows)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "Error al cargar notificaciones")
                }
            } else {
                NetworkResult.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun obtenerContadorNoLeidas(): NetworkResult<Int> {
        return try {
            val response = notificationsService.getContadorNoLeidas()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    NetworkResult.Success(body.data.total)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "Error al obtener el contador")
                }
            } else {
                NetworkResult.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun marcarLeida(id: String): NetworkResult<Unit> {
        return try {
            val response = notificationsService.marcarLeida(id)
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error("Error del servidor (${response.code()})")
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun marcarTodasLeidas(): NetworkResult<Unit> {
        return try {
            val response = notificationsService.marcarTodasLeidas()
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error("Error del servidor (${response.code()})")
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }
}