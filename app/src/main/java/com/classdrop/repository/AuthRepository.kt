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
                    // CAMBIO: Accedemos al mensaje dentro del objeto de error
                    NetworkResult.Error(body?.error?.message ?: "Error desconocido")
                }
            } else {
                NetworkResult.Error("Credenciales inválidas o error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }
    
    suspend fun register(nombre: String, correo: String, contrasena: String): NetworkResult<RegisterResponse> {
        return try {
            val request = RegisterRequest(nombre.trim(), correo.trim().lowercase(), contrasena)
            val response = authService.register(request)



            //isSuccessful acepta tanto 200 (OK) como 201 (Created)
            if (response.isSuccessful) {
                val body = response.body()

                // Verificación defensiva del cuerpo de respuesta mapeado de apiResponse.js
                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    // CAMBIO: Accedemos al mensaje dentro del objeto de error
                    NetworkResult.Error(body?.error?.message ?: "Error desconocido")
                }
            } else {
                // Si el código es 404, 400, 500, etc. extraeremos el mensaje real del errorBody
                val errorResponseBody = response.errorBody()?.string()
                NetworkResult.Error("Error del servidor (${response.code()}): $errorResponseBody")
            }
        } catch (e: Exception) {
            NetworkResult.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }
}
