package com.classdrop.network

import com.classdrop.model.ApiResponse
import com.classdrop.model.ArchivosPaginados
import com.classdrop.model.CrearArchivoRequest
import com.classdrop.model.FileModel
import retrofit2.Response
import retrofit2.http.*

interface FilesService {

    @GET("archivos/publicados")
    suspend fun getArchivosPublicados(
        @Query("materiaId") materiaId: String? = null,
        @Query("search") search: String? = null,
        @Query("limite") limite: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ApiResponse<ArchivosPaginados>>

    @GET("archivos/me")
    suspend fun getMisArchivos(
        @Query("estado") estado: String? = null
    ): Response<ApiResponse<ArchivosPaginados>>

    @GET("archivos/{id}")
    suspend fun getArchivoPorId(@Path("id") id: String): Response<ApiResponse<FileModel>>

    @POST("archivos")
    suspend fun crearArchivo(@Body request: CrearArchivoRequest): Response<ApiResponse<FileModel>>

    @DELETE("archivos/{id}")
    suspend fun eliminarArchivo(@Path("id") id: String): Response<Unit>

    @POST("likes/archivos/{archivoId}")
    suspend fun darLike(@Path("archivoId") archivoId: String): Response<Unit>

    @DELETE("likes/archivos/{archivoId}")
    suspend fun quitarLike(@Path("archivoId") archivoId: String): Response<Unit>

    @POST("dislikes/archivos/{archivoId}")
    suspend fun darDislike(@Path("archivoId") archivoId: String): Response<Unit>

    @DELETE("dislikes/archivos/{archivoId}")
    suspend fun quitarDislike(@Path("archivoId") archivoId: String): Response<Unit>
}