package com.classdrop.network

import com.classdrop.model.CrearArchivoRequest
import com.classdrop.model.FileModel
import retrofit2.Response
import retrofit2.http.*

interface FilesService {

    @GET("api/archivos/publicados")
    suspend fun getArchivosPublicados(
        @Query("materiaId") materiaId: String? = null,
        @Query("search") search: String? = null,
        @Query("limite") limite: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<List<FileModel>>

    @GET("api/archivos/mis-archivos")
    suspend fun getMisArchivos(
        @Query("estado") estado: String? = null
    ): Response<List<FileModel>>

    @POST("api/archivos")
    suspend fun crearArchivo(@Body request: CrearArchivoRequest): Response<FileModel>

    @DELETE("api/archivos/{id}")
    suspend fun eliminarArchivo(@Path("id") id: String): Response<Unit>
}