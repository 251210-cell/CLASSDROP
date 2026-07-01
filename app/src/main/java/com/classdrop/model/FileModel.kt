package com.classdrop.model

data class Adjunto(
    val urlStorage: String,
    val nombreOriginal: String,
    val tipoMime: String,
    val tamanoBytes: Long,
    val numPaginas: Int? = null
)

data class CrearArchivoRequest(
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val materiaId: String,
    val adjuntos: List<Adjunto>
)

data class FileModel(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val materiaId: String,
    val adjuntos: List<Adjunto> = emptyList()
)