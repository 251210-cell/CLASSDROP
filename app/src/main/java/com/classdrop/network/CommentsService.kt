// app/src/main/java/com/classdrop/network/CommentsService.kt
package com.classdrop.network

import com.classdrop.model.ApiResponse
import com.classdrop.model.Comment // Asegúrate de tener este modelo creado
import retrofit2.Response
import retrofit2.http.*

interface CommentsService {

    // 1. Listar comentarios por archivo
    @GET("comentarios/archivo/{archivoId}")
    suspend fun getComentariosByArchivo(
        @Path("archivoId") archivoId: String
    ): Response<ApiResponse<List<Comment>>>

    // 2. Crear un nuevo comentario (El AuthInterceptor inyectará el token automáticamente)
    @POST("comentarios")
    suspend fun crearComentario(
        @Body body: Map<String, String>
    ): Response<ApiResponse<Comment>>

    // 3. Eliminar comentario (Retorna un 204 No Content, por lo que Response<Unit> es ideal)
    @DELETE("comentarios/{id}")
    suspend fun eliminarComentario(
        @Path("id") id: String
    ): Response<Unit>
}