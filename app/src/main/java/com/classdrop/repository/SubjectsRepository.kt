package com.classdrop.repository

import android.content.Context
import com.classdrop.model.CreateMateriaRequest
import com.classdrop.model.CuatrimestreResponse
import com.classdrop.model.MateriaResponse
import com.classdrop.network.RetrofitClient
import com.classdrop.network.SubjectsService
import retrofit2.Response

class SubjectsRepository(context: Context) {

    private val subjectsService: SubjectsService =
        RetrofitClient.create(context).create(SubjectsService::class.java)

    suspend fun getAllMaterias(search: String? = null, limit: Int? = null): Response<List<MateriaResponse>> {
        val response = subjectsService.getAllMaterias(search, limit)
        return desenvolverLista(response)
    }

    suspend fun getMateriasByCuatrimestre(cuatrimestreId: Int): Response<List<MateriaResponse>> {
        val response = subjectsService.getMateriasByCuatrimestre(cuatrimestreId)
        return desenvolverLista(response)
    }

    suspend fun getCuatrimestres(): Response<List<CuatrimestreResponse>> {
        val response = subjectsService.getCuatrimestres()
        return desenvolverLista(response)
    }

    suspend fun createMateria(nombre: String, cuatrimestreId: Int, icono: String? = null): Response<MateriaResponse> {
        val request = CreateMateriaRequest(nombre, icono, cuatrimestreId)
        val response = subjectsService.createMateria(request)
        return desenvolverObjeto(response)
    }

    suspend fun deleteMateria(id: String): Response<Unit> {
        return subjectsService.deleteMateria(id)
    }

    // --- Helpers para desenvolver el wrapper {success, data, meta, error} de tu API ---

    private fun <T> desenvolverLista(response: Response<com.classdrop.model.ApiResponse<List<T>>>): Response<List<T>> {
        val body = response.body()
        return if (response.isSuccessful && body?.success == true && body.data != null) {
            Response.success(body.data, response.raw())
        } else {
            Response.error(response.code(), response.errorBody() ?: okhttp3.ResponseBody.create(null, body?.error ?: "Error"))
        }
    }

    private fun <T> desenvolverObjeto(response: Response<com.classdrop.model.ApiResponse<T>>): Response<T> {
        val body = response.body()
        return if (response.isSuccessful && body?.success == true && body.data != null) {
            Response.success(body.data, response.raw())
        } else {
            Response.error(response.code(), response.errorBody() ?: okhttp3.ResponseBody.create(null, body?.error ?: "Error"))
        }
    }
}