// app/src/main/java/com/classdrop/network/PoliticaService.kt
package com.classdrop.network

import com.classdrop.model.ActualizarPoliticaRequest
import com.classdrop.model.ApiResponse
import com.classdrop.model.CrearPoliticaRequest
import com.classdrop.model.Politica
import retrofit2.Response
import retrofit2.http.*

interface PoliticaService {

    // Listar políticas por categoría (pública, no requiere token)
    @GET("politicas")
    suspend fun listarPoliticas(
        @Query("categoria") categoria: String? = null
    ): Response<ApiResponse<List<Politica>>>

    // Mensaje principal vigente (pública). Si aún no hay ninguna, el backend responde 404.
    @GET("politicas/principal")
    suspend fun obtenerPrincipal(): Response<ApiResponse<Politica>>

    // Crear política nueva (solo admin, el AuthInterceptor ya inyecta el token)
    @POST("politicas")
    suspend fun crearPolitica(@Body request: CrearPoliticaRequest): Response<ApiResponse<Politica>>

    // Actualizar política existente (solo admin)
    @PUT("politicas/{id}")
    suspend fun actualizarPolitica(
        @Path("id") id: String,
        @Body request: ActualizarPoliticaRequest
    ): Response<ApiResponse<Politica>>

    // Eliminar (borrado lógico: el backend la marca como inactiva) (solo admin)
    @DELETE("politicas/{id}")
    suspend fun eliminarPolitica(@Path("id") id: String): Response<Unit>
}