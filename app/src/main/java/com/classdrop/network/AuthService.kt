package com.classdrop.network


import com.classdrop.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    @POST("auth/registro")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse>>

    @POST("auth/registro/admin")
    suspend fun registerAdmin(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse>>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @GET("auth/perfil")
    suspend fun getPerfil(): Response<ApiResponse<User>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Map<String, String>>>

    @PUT("auth/fcm-token")
    suspend fun actualizarFcmToken(
        @Body body: Map<String, String>
    ): Response<ApiResponse<Map<String, String>>>

    // ==========================================
    //   RUTAS PARA VERIFICACIÓN EN 2 PASOS (2FA)
    // ==========================================

    // 1. Generar la clave secreta de 6 dígitos
    @POST("auth/2fa/generar")
    suspend fun generar2FA(): Response<ApiResponse<Map<String, Any>>>

    // 2. Verificar el primer código para activar el 2FA
    @POST("auth/2fa/activar")
    suspend fun activar2FA(
        @Body body: Map<String, String>
    ): Response<ApiResponse<Map<String, Any>>>

    // 3. Paso 2 del Login: Verificar código de 6 dígitos
    @POST("auth/login/2fa")
    suspend fun verify2FA(
        @Body request: Verify2FARequest
    ): Response<ApiResponse<LoginResponse>>
}