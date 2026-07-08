package com.classdrop.model

import com.google.gson.annotations.SerializedName

// Forma exacta en la que llega la notificación desde el backend.
data class NotificationDto(
    val id: String,
    val titulo: String,
    val cuerpo: String,
    val tipo: String, // "info" | "exito" | "error" | "advertencia"
    val archivoId: String? = null,
    val leida: Boolean = false,
    @SerializedName("creado_en") val creadoEn: String? = null
)

data class NotificacionesPaginadas(
    val count: Int,
    val rows: List<NotificationDto>
)

data class ContadorNotificaciones(
    val total: Int
)