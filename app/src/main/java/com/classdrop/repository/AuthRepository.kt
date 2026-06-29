package com.classdrop.repository

import com.classdrop.model.LoginResponse
import com.classdrop.model.User
import com.classdrop.model.UserRole
import com.classdrop.network.AuthService
import com.classdrop.network.NetworkResult
import kotlinx.coroutines.delay

class AuthRepository(private val authService: AuthService) {

    suspend fun login(email: String, password: String): NetworkResult<LoginResponse> {
        val cleanEmail = email.trim().lowercase()
        
        // Simulamos un retraso de red para que se vea el ProgressBar
        delay(1000)

        // MODO OFFLINE: Si el dominio es @classdrop.com, entra como Admin. Si no, como Estudiante.
        return if (cleanEmail.endsWith("@classdrop.com")) {
            NetworkResult.Success(
                LoginResponse("token-falso-admin", User("1", "Administrador", cleanEmail, UserRole.ADMIN))
            )
        } else {
            NetworkResult.Success(
                LoginResponse("token-falso-estudiante", User("2", "Estudiante", cleanEmail, UserRole.STUDENT))
            )
        }
    }
}
