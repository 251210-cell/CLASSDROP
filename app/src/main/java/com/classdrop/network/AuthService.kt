package com.classdrop.network


import com.classdrop.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @POST("auth/registro")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse>>

    // El AuthInterceptor ya manda el Bearer token automáticamente, así que
    // el backend sabe exactamente qué token revocar sin que se lo mandemos aparte.
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @PUT("auth/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): retrofit2.Response<ApiResponse<Unit>>
}