// app/src/main/java/com/classdrop/network/NormaService.kt
package com.classdrop.network

import com.classdrop.model.ActualizarNormaRequest
import com.classdrop.model.ApiResponse
import com.classdrop.model.CrearNormaRequest
import com.classdrop.model.Norma
import retrofit2.Response
import retrofit2.http.*

interface NormaService {

    // Listar todas las normas, opcionalmente filtradas por estado (pública, no requiere token)
    @GET("normas")
    suspend fun listarNormas(
        @Query("estado") estado: String? = null
    ): Response<ApiResponse<List<Norma>>>

    // Solo las normas activas (pública, no requiere token)
    @GET("normas/activas")
    suspend fun listarNormasActivas(): Response<ApiResponse<List<Norma>>>

    // Crear norma nueva (solo admin, el AuthInterceptor ya inyecta el token)
    @POST("normas")
    suspend fun crearNorma(@Body request: CrearNormaRequest): Response<ApiResponse<Norma>>

    // Actualizar norma existente (solo admin)
    @PUT("normas/{id}")
    suspend fun actualizarNorma(
        @Path("id") id: String,
        @Body request: ActualizarNormaRequest
    ): Response<ApiResponse<Norma>>

    // Eliminar (borrado real en este recurso) (solo admin)
    @DELETE("normas/{id}")
    suspend fun eliminarNorma(@Path("id") id: String): Response<Unit>
}