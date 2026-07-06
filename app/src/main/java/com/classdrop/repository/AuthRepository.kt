package com.classdrop.repository

import com.classdrop.model.*
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
                    NetworkResult.Error(body?.error?.message ?: "Error desconocido")
                }
            } else {
                NetworkResult.Error("Credenciales inválidas o error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }

    suspend fun verify2FA(userId: String, code: String): NetworkResult<LoginResponse> {
        return try {
            val request = Verify2FARequest(userId, code)
            val response = authService.verify2FA(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "Código incorrecto")
                }
            } else {
                NetworkResult.Error("Error en la verificación (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Fallo de conexión: ${e.message}")
        }
    }

    // --- MÉTODOS PARA ACTIVACIÓN DESDE PERFIL ---

    suspend fun generar2FACodigo(): NetworkResult<String> {
        return try {
            val response = authService.generar2FA()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                NetworkResult.Success(body.data?.get("mensaje")?.toString() ?: "Código enviado")
            } else {
                NetworkResult.Error(body?.error?.message ?: "No se pudo generar el código")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.message}")
        }
    }

    suspend fun activar2FA(code: String): NetworkResult<String> {
        return try {
            val bodyMap = mapOf("token" to code)
            val response = authService.activar2FA(bodyMap)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                NetworkResult.Success(body.data?.get("mensaje")?.toString() ?: "Activado")
            } else {
                NetworkResult.Error(body?.error?.message ?: "Código incorrecto")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.message}")
        }
    }

    suspend fun register(nombre: String, correo: String, contrasena: String): NetworkResult<RegisterResponse> {
        return try {
            val request = RegisterRequest(nombre.trim(), correo.trim().lowercase(), contrasena)
            val response = authService.register(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "Error desconocido")
                }
            } else {
                val errorResponseBody = response.errorBody()?.string()
                NetworkResult.Error("Error del servidor (${response.code()}): $errorResponseBody")
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }

    suspend fun logout(): NetworkResult<Unit> {
        return try {
            val response = authService.logout() 
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("El servidor no pudo cerrar la sesión (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }
}
