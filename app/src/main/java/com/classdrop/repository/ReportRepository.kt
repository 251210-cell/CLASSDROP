package com.classdrop.repository

import android.content.Context
import com.classdrop.model.Reporte
import com.classdrop.model.ResolverReporteRequest
import com.classdrop.network.ReporteService
import com.classdrop.network.RetrofitClient

class ReportRepository(
    context: Context,
    private val reporteService: ReporteService = RetrofitClient.create(context).create(ReporteService::class.java)
) {

    suspend fun obtenerPendientes(): Result<List<Reporte>> {
        return try {
            val response = reporteService.listarPendientes()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Error API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** estado = "descartado" -> visto bueno, se restaura el contenido.
     *  estado = "resuelto"   -> visto malo, se borra el contenido definitivamente. */
    suspend fun resolver(reporteId: String, estado: String, accionTomada: String? = null): Result<Unit> {
        return try {
            val response = reporteService.resolver(reporteId, ResolverReporteRequest(estado, accionTomada))
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Error API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}