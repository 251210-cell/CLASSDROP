package com.classdrop.model

data class LoginResponse(
    val token: String? = null,
    val usuario: User? = null,
    
    // Campos para 2FA detectados en tu auth.controller.js
    val requires2FA: Boolean = false,
    val userId: String? = null,
    val mensaje: String? = null
)
