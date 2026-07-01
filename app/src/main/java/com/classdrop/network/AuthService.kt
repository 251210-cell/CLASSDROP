package com.classdrop.network

import com.classdrop.model.ApiResponse
import com.classdrop.model.LoginRequest
import com.classdrop.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
}