package com.classdrop.network

import com.classdrop.model.ApiResponse
import com.classdrop.model.Reporte
import com.classdrop.model.ResolverReporteRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT

interface ReporteService {

    @GET("reportes/pendientes")
    suspend fun listarPendientes(): Response<ApiResponse<List<Reporte>>>

    @PUT("reportes/{id}/resolver")
    suspend fun resolver(
        @Path("id") reporteId: String,
        @Body body: ResolverReporteRequest
    ): Response<ApiResponse<Map<String, Any?>>>
}