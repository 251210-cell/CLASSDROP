package com.classdrop.network

import com.classdrop.model.ApiResponse
import com.classdrop.model.ContadorNotificaciones
import com.classdrop.model.NotificacionesPaginadas
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsService {

    @GET("notificaciones")
    suspend fun getNotificaciones(
        @Query("limite") limite: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ApiResponse<NotificacionesPaginadas>>

    @GET("notificaciones/no-leidas/contador")
    suspend fun getContadorNoLeidas(): Response<ApiResponse<ContadorNotificaciones>>

    @PATCH("notificaciones/{id}/leer")
    suspend fun marcarLeida(@Path("id") id: String): Response<Unit>

    @PATCH("notificaciones/leer-todas")
    suspend fun marcarTodasLeidas(): Response<Unit>
}