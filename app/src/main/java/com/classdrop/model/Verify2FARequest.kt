package com.classdrop.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la verificación en dos pasos.
 * Sincronizado exactamente con AuthController.login2FA del backend.
 */
data class Verify2FARequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("tokenVerificacion") val tokenVerificacion: String, // 👈 Nombre corregido
    @SerializedName("rememberMe") val rememberMe: Boolean = false
)
