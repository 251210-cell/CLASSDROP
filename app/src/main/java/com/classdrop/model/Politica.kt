package com.classdrop.model

import com.google.gson.annotations.SerializedName

/**
 * Representa una fila real de la tabla `politicas` en el backend
 * (endpoint /api/v1/politicas). Se usa solo en la capa de red/repositorio;
 * la UI sigue trabajando con CommunityRule como hasta ahora.
 */
data class Politica(
    val id: String,
    val categoria: String = "general",
    val titulo: String,
    val contenido: String,
    val icono: String? = null,
    val esPrincipal: Boolean = false,
    val orden: Int = 0,
    val activo: Boolean = true,
    @SerializedName("creado_en") val creadoEn: String? = null,
    @SerializedName("actualizado_en") val actualizadoEn: String? = null
)

data class CrearPoliticaRequest(
    val categoria: String,
    val titulo: String,
    val contenido: String,
    val esPrincipal: Boolean = false
)

data class ActualizarPoliticaRequest(
    val titulo: String? = null,
    val contenido: String? = null
)