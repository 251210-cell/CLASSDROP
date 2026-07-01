package com.classdrop.repository

import com.classdrop.model.LoginRequest
import com.classdrop.model.LoginResponse
import com.classdrop.network.AuthService
import com.classdrop.network.NetworkResult

class AuthRepository(private val authService: AuthService) {

    suspend fun login(correo: String, contrasena: String): NetworkResult<LoginResponse> {
        return try {
            val response = authService.login(LoginRequest(correo.trim().lowercase(), contrasena))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.error ?: "Error desconocido")
                }
            } else {
                NetworkResult.Error("Credenciales inválidas o error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }
}
