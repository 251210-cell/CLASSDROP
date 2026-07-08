package com.classdrop.model

import com.google.gson.annotations.SerializedName

data class ReportadorInfo(
    val id: String,
    val nombreCompleto: String
)

/**
 * Refleja exactamente lo que devuelve GET /api/v1/reportes/pendientes.
 * archivo/comentario vienen anidados completos (con autor) SOLO cuando
 * tipoContenido corresponde; el otro siempre llega null.
 */
data class Reporte(
    val id: String,
    val reportadoPor: String? = null,
    val tipoContenido: String, // "archivo" | "comentario"
    val archivoId: String? = null,
    val comentarioId: String? = null,
    val puntuacion: Int? = null,
    val estado: String = "pendiente", // "pendiente" | "resuelto" | "descartado"
    val totalDislikes: Int = 0,
    val reportador: ReportadorInfo? = null,
    val archivo: FileModel? = null,
    val comentario: Comment? = null,
    @SerializedName("creado_en") val creadoEn: String? = null
)

data class ResolverReporteRequest(
    val estado: String, // "resuelto" | "descartado"
    val accionTomada: String? = null
)