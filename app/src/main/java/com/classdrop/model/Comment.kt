package com.classdrop.model

import com.google.gson.annotations.SerializedName

data class ComentarioAutor(
    val id: String,
    val nombreCompleto: String,
    val avatarUrl: String? = null
)

data class Comment(
    val id: String,
    val contenido: String,
    val archivoId: String? = null,
    val usuarioId: String? = null,
    val autor: ComentarioAutor? = null,
    @SerializedName("creado_en") val creadoEn: String? = null,
    var totalLikes: Int = 0,
    var totalDislikes: Int = 0,
    var isLiked: Boolean = false,
    var isDisliked: Boolean = false
)