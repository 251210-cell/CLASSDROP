package com.classdrop.model

import com.google.gson.annotations.SerializedName
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

data class AutorArchivo(
    val id: String,
    val nombreCompleto: String
)

data class MateriaArchivo(
    val id: String,
    val nombre: String,
    val icono: String? = null
)



data class FileModel(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val materiaId: String,
    val adjuntos: List<Adjunto> = emptyList(),
    val autor: AutorArchivo? = null,
    val materia: MateriaArchivo? = null,
    val totalLikes: Int = 0,
    val totalDislikes: Int = 0,
    val totalDescargas: Int = 0,
    val totalComentarios: Int = 0,
    @SerializedName("creado_en") val creadoEn: String? = null
)

data class ArchivosPaginados(
    val count: Int,
    val rows: List<FileModel>
)