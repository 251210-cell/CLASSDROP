// app/src/main/java/com/classdrop/repository/PoliticaRepository.kt
package com.classdrop.repository

import android.content.Context
import com.classdrop.model.ActualizarPoliticaRequest
import com.classdrop.model.CommunityRule
import com.classdrop.model.CrearPoliticaRequest
import com.classdrop.model.Politica
import com.classdrop.network.NetworkResult
import com.classdrop.network.PoliticaService
import com.classdrop.network.RetrofitClient

/**
 * Conecta las pantallas de "Política de Privacidad" (admin y estudiante) con el
 * endpoint real /api/v1/politicas. Antes esto vivía solo en Room/SharedPreferences,
 * por lo que los cambios del admin nunca llegaban a los demás dispositivos.
 */
class PoliticaRepository(context: Context) {

    private val service: PoliticaService =
        RetrofitClient.create(context).create(PoliticaService::class.java)

    companion object {
        private const val CATEGORIA_PRIVACIDAD = "privacidad"
    }

    // --- Lectura (pública, sin login) ---

    suspend fun listarReglas(): NetworkResult<List<CommunityRule>> {
        return try {
            val response = service.listarPoliticas(CATEGORIA_PRIVACIDAD)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val reglas = body.data
                        .filter { !it.esPrincipal } // el mensaje principal se pide aparte
                        .sortedBy { it.orden }
                        .map { it.toCommunityRule() }
                    NetworkResult.Success(reglas)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "No se pudieron cargar las políticas")
                }
            } else {
                NetworkResult.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun obtenerMensajePrincipal(): NetworkResult<CommunityRule?> {
        return try {
            val response = service.obtenerPrincipal()
            when {
                response.isSuccessful -> NetworkResult.Success(response.body()?.data?.toCommunityRule())
                response.code() == 404 -> NetworkResult.Success(null) // aún no hay mensaje principal
                else -> NetworkResult.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    // --- Escritura (solo admin, requiere token) ---

    suspend fun crearRegla(titulo: String, descripcion: String): NetworkResult<CommunityRule> =
        crear(titulo, descripcion, esPrincipal = false)

    suspend fun crearMensajePrincipal(descripcion: String): NetworkResult<CommunityRule> =
        crear(titulo = "Mensaje principal", descripcion = descripcion, esPrincipal = true)

    private suspend fun crear(titulo: String, descripcion: String, esPrincipal: Boolean): NetworkResult<CommunityRule> {
        return try {
            val request = CrearPoliticaRequest(
                categoria = CATEGORIA_PRIVACIDAD,
                titulo = titulo,
                contenido = descripcion,
                esPrincipal = esPrincipal
            )
            val response = service.crearPolitica(request)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) NetworkResult.Success(data.toCommunityRule())
                else NetworkResult.Error("No se pudo crear la política")
            } else {
                NetworkResult.Error(mensajeError(response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun actualizarRegla(id: String, titulo: String, descripcion: String): NetworkResult<CommunityRule> =
        actualizar(id, ActualizarPoliticaRequest(titulo = titulo, contenido = descripcion))

    suspend fun actualizarMensajePrincipal(id: String, descripcion: String): NetworkResult<CommunityRule> =
        actualizar(id, ActualizarPoliticaRequest(contenido = descripcion))

    private suspend fun actualizar(id: String, request: ActualizarPoliticaRequest): NetworkResult<CommunityRule> {
        return try {
            val response = service.actualizarPolitica(id, request)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) NetworkResult.Success(data.toCommunityRule())
                else NetworkResult.Error("No se pudo actualizar la política")
            } else {
                NetworkResult.Error(mensajeError(response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun eliminarRegla(id: String): NetworkResult<Unit> {
        return try {
            val response = service.eliminarPolitica(id)
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error(mensajeError(response.code()))
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    private fun mensajeError(code: Int): String = when (code) {
        401 -> "Tu sesión expiró, inicia sesión de nuevo"
        403 -> "Se requiere una cuenta de administrador para esta acción"
        404 -> "La política no existe o ya fue eliminada"
        422 -> "El título y la descripción no pueden estar vacíos"
        else -> "Error del servidor ($code)"
    }

    private fun Politica.toCommunityRule() = CommunityRule(
        id = id,
        number = orden,
        title = titulo,
        description = contenido,
        iconResName = icono ?: "ic_status_shield",
        lastEdited = actualizadoEn ?: creadoEn ?: "",
        isActive = activo,
        adminNote = ""
    )
}