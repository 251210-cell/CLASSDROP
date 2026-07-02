package com.classdrop.model

data class RegisterRequest(
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String
)