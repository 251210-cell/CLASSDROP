package com.classdrop.model

import com.google.gson.annotations.SerializedName

// Modelo para Materia (como viene de la API)
data class MateriaResponse(
    val id: String,
    val nombre: String,
    val icono: String?,
    val cuatrimestreId: Int,
    val activo: Boolean,
    @SerializedName("totalArchivos") val fileCount: Int? = 0
)