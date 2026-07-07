package com.classdrop.network

import com.classdrop.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    @POST("auth/registro")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Map<String, String>>>

    @PUT("auth/fcm-token")
    suspend fun actualizarFcmToken(@Body body: Map<String, String>): Response<ApiResponse<Map<String, String>>>

    @POST("auth/2fa/generar")
    suspend fun generar2FA(): Response<ApiResponse<Map<String, Any>>>

    @POST("auth/2fa/activar")
    suspend fun activar2FA(@Body body: Map<String, String>): Response<ApiResponse<Map<String, Any>>>

    @POST("auth/login/2fa")
    suspend fun verify2FA(@Body request: Verify2FARequest): Response<ApiResponse<LoginResponse>>
}
