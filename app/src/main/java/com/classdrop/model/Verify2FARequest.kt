package com.classdrop.model

/**
 * Modelo para la verificación en dos pasos.
 * Coincide con lo esperado en AuthController.login2FA del backend.
 */
data class Verify2FARequest(
    val userId: String,
    val token: String // Este es el código de 6 dígitos
)
