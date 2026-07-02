package com.classdrop.model

data class CreateMateriaRequest(
    val nombre: String,
    val icono: String? = null,
    val cuatrimestreId: Int
)