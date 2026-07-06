package com.classdrop.model

import com.google.gson.annotations.SerializedName

/**
 * Representa una fila real de la tabla `normas` en el backend
 * (endpoint /api/v1/normas). Se usa solo en la capa de red/repositorio;
 * la UI sigue trabajando con CommunityRule como hasta ahora.
 */
data class Norma(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val icono: String? = null,
    val estado: String = "activa",
    @SerializedName("creado_en") val creadoEn: String? = null,
    @SerializedName("actualizado_en") val actualizadoEn: String? = null
)

data class CrearNormaRequest(
    val titulo: String,
    val descripcion: String
)

data class ActualizarNormaRequest(
    val titulo: String? = null,
    val descripcion: String? = null
)