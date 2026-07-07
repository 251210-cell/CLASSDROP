package com.classdrop.repository

import com.classdrop.model.*
import com.classdrop.network.AuthService
import com.classdrop.network.NetworkResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
                parseError<LoginResponse>(response)
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }

    /**
     * Verifica el código de 2FA. 
     * Se añade rememberMe = true por defecto para registrar el dispositivo como de confianza.
     */
    suspend fun verify2FA(userId: String, code: String, rememberMe: Boolean = true): NetworkResult<LoginResponse> {
        return try {
            val request = Verify2FARequest(
                userId = userId,
                tokenVerificacion = code,
                rememberMe = rememberMe
            )
            val response = authService.verify2FA(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "Código incorrecto")
                }
            } else {
                parseError<LoginResponse>(response)
            }
        } catch (e: Exception) {
            NetworkResult.Error("Fallo de conexión: ${e.message}")
        }
    }

    suspend fun generar2FACodigo(): NetworkResult<String> {
        return try {
            val response = authService.generar2FA()
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                NetworkResult.Success(body.data?.get("mensaje")?.toString() ?: "Código enviado")
            } else {
                parseError<String>(response)
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.message}")
        }
    }

    suspend fun activar2FA(code: String): NetworkResult<String> {
        return try {
            val bodyMap = mapOf("tokenVerificacion" to code)
            val response = authService.activar2FA(bodyMap)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                NetworkResult.Success(body.data?.get("mensaje")?.toString() ?: "Activado")
            } else {
                parseError<String>(response)
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
                parseError<RegisterResponse>(response)
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
                parseError<Unit>(response)
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }

    private fun <T> parseError(response: retrofit2.Response<*>): NetworkResult<T> {
        val errorBody = response.errorBody()?.string()
        val message = try {
            val type = object : TypeToken<ApiResponse<Any>>() {}.type
            val apiResponse: ApiResponse<Any> = Gson().fromJson(errorBody, type)
            apiResponse.error?.message ?: "Error del servidor (${response.code()})"
        } catch (e: Exception) {
            "Error del servidor (${response.code()})"
        }
        return NetworkResult.Error(message)
    }
}
