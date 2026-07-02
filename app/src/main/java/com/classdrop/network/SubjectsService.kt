package com.classdrop.network

import com.classdrop.model.ApiResponse
import com.classdrop.model.CreateMateriaRequest
import com.classdrop.model.CuatrimestreResponse
import com.classdrop.model.MateriaResponse
import retrofit2.Response
import retrofit2.http.*

interface SubjectsService {

    // === CUATRIMESTRES ===

    @GET("cuatrimestres")
    suspend fun getCuatrimestres(): Response<ApiResponse<List<CuatrimestreResponse>>>

    @GET("cuatrimestres/{id}")
    suspend fun getCuatrimestreById(@Path("id") id: Int): Response<ApiResponse<CuatrimestreResponse>>


    // === MATERIAS ===

    @GET("materias")
    suspend fun getAllMaterias(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<List<MateriaResponse>>>

    @GET("materias/cuatrimestre/{cuatrimestreId}")
    suspend fun getMateriasByCuatrimestre(
        @Path("cuatrimestreId") cuatrimestreId: Int
    ): Response<ApiResponse<List<MateriaResponse>>>

    @GET("materias/{id}")
    suspend fun getMateriaById(@Path("id") id: String): Response<ApiResponse<MateriaResponse>>

    @POST("materias")
    suspend fun createMateria(@Body request: CreateMateriaRequest): Response<ApiResponse<MateriaResponse>>

    @PUT("materias/{id}")
    suspend fun updateMateria(
        @Path("id") id: String,
        @Body campos: Map<String, @JvmSuppressWildcards Any>
    ): Response<ApiResponse<MateriaResponse>>

    @DELETE("materias/{id}")
    suspend fun deleteMateria(@Path("id") id: String): Response<Unit>
}