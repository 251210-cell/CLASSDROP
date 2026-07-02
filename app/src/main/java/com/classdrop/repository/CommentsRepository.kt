// app/src/main/java/com/classdrop/repository/CommentsRepository.kt
package com.classdrop.repository

import android.content.Context
import com.classdrop.model.Comment
import com.classdrop.network.NetworkResult
import com.classdrop.network.RetrofitClient
import com.classdrop.network.CommentsService

class CommentsRepository(context: Context) {

    private val commentsService: CommentsService =
        RetrofitClient.create(context).create(CommentsService::class.java)

    // Obtener comentarios
    suspend fun obtenerComentarios(archivoId: String): NetworkResult<List<Comment>> {
        return try {
            val response = commentsService.getComentariosByArchivo(archivoId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "Error al cargar comentarios")
                }
            } else {
                NetworkResult.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    // Publicar un nuevo comentario
    suspend fun publicarComentario(archivoId: String, contenido: String): NetworkResult<Comment> {
        return try {
            val campoData = mapOf("archivoId" to archivoId, "contenido" to contenido)
            val response = commentsService.crearComentario(campoData)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.error?.message ?: "Error al publicar comentario")
                }
            } else {
                NetworkResult.Error("No se pudo publicar el comentario (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }

    // Eliminar un comentario
    suspend fun borrarComentario(comentarioId: String): NetworkResult<Unit> {
        return try {
            val response = commentsService.eliminarComentario(comentarioId)
            // Como DELETE responde con 204 No Content, response.isSuccessful será true y body estará vacío.
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("No tienes permisos o el comentario no existe (${response.code()})")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error de conexión: ${e.message}")
        }
    }
}