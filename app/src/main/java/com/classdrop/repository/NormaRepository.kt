// app/src/main/java/com/classdrop/repository/NormaRepository.kt
package com.classdrop.repository

import android.content.Context
import com.classdrop.model.ActualizarNormaRequest
import com.classdrop.model.CommunityRule
import com.classdrop.model.CrearNormaRequest
import com.classdrop.model.Norma
import com.classdrop.network.NetworkResult
import com.classdrop.network.NormaService
import com.classdrop.network.RetrofitClient

/**
 * Conecta las pantallas de "Normas de la Comunidad" (admin y estudiante) con el
 * endpoint real /api/v1/normas. Antes vivía solo en SharedPreferences, por lo
 * que los cambios del admin nunca llegaban a los demás dispositivos.
 *
 * El backend de Norma no tiene un campo especial para el "Régimen Sancionatorio"
 * (a diferencia de Política, que sí tiene `esPrincipal`), así que aquí lo
 * identificamos por su título fijo para no tener que tocar el backend.
 */
class NormaRepository(context: Context) {

    private val service: NormaService =
        RetrofitClient.create(context).create(NormaService::class.java)

    companion object {
        const val TITULO_SANCIONES = "Régimen Sancionatorio"
    }

    // --- Lectura (pública, sin login) ---

    suspend fun listarReglas(): NetworkResult<List<CommunityRule>> {
        return try {
            val response = service.listarNormasActivas()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val reglas = body.data
                        .filter { !it.titulo.equals(TITULO_SANCIONES, ignoreCase = true) }
                        .map { it.toCommunityRule() }
                    NetworkResult.Success(reglas)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "No se pudieron cargar las normas")
                }
            } else {
                NetworkResult.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun obtenerSanciones(): NetworkResult<CommunityRule?> {
        return try {
            val response = service.listarNormasActivas()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val sanciones = body.data.firstOrNull {
                        it.titulo.equals(TITULO_SANCIONES, ignoreCase = true)
                    }
                    NetworkResult.Success(sanciones?.toCommunityRule())
                } else {
                    NetworkResult.Error(body?.error?.message ?: "No se pudo cargar el régimen sancionatorio")
                }
            } else {
                NetworkResult.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    // --- Escritura (solo admin, requiere token) ---

    suspend fun crearRegla(titulo: String, descripcion: String): NetworkResult<CommunityRule> =
        crear(titulo, descripcion)

    suspend fun crearSanciones(descripcion: String): NetworkResult<CommunityRule> =
        crear(TITULO_SANCIONES, descripcion)

    private suspend fun crear(titulo: String, descripcion: String): NetworkResult<CommunityRule> {
        return try {
            val response = service.crearNorma(CrearNormaRequest(titulo = titulo, descripcion = descripcion))
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) NetworkResult.Success(data.toCommunityRule())
                else NetworkResult.Error("No se pudo crear la norma")
            } else {
                NetworkResult.Error(mensajeError(response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun actualizarRegla(id: String, titulo: String, descripcion: String): NetworkResult<CommunityRule> =
        actualizar(id, ActualizarNormaRequest(titulo = titulo, descripcion = descripcion))

    suspend fun actualizarSanciones(id: String, descripcion: String): NetworkResult<CommunityRule> =
        actualizar(id, ActualizarNormaRequest(descripcion = descripcion))

    private suspend fun actualizar(id: String, request: ActualizarNormaRequest): NetworkResult<CommunityRule> {
        return try {
            val response = service.actualizarNorma(id, request)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) NetworkResult.Success(data.toCommunityRule())
                else NetworkResult.Error("No se pudo actualizar la norma")
            } else {
                NetworkResult.Error(mensajeError(response.code()))
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun eliminarRegla(id: String): NetworkResult<Unit> {
        return try {
            val response = service.eliminarNorma(id)
            if (response.isSuccessful) NetworkResult.Success(Unit)
            else NetworkResult.Error(mensajeError(response.code()))
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    private fun mensajeError(code: Int): String = when (code) {
        401 -> "Tu sesión expiró, inicia sesión de nuevo"
        403 -> "Se requiere una cuenta de administrador para esta acción"
        404 -> "La norma no existe o ya fue eliminada"
        else -> "Error del servidor ($code)"
    }

    private fun Norma.toCommunityRule() = CommunityRule(
        id = id,
        title = titulo,
        description = descripcion,
        iconResName = icono ?: "ic_status_shield",
        lastEdited = actualizadoEn ?: creadoEn ?: "",
        isActive = estado == "activa",
        adminNote = ""
    )
}