package com.classdrop.repository

import android.content.Context
import android.net.Uri
import com.classdrop.model.Adjunto
import com.classdrop.model.CrearArchivoRequest
import com.classdrop.model.FileModel
import com.classdrop.model.GuardadoResponse
import com.classdrop.network.FilesService
import com.classdrop.network.RetrofitClient
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FilesRepository(
    private val context: Context,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val filesService: FilesService = RetrofitClient.create(context).create(FilesService::class.java)
) {

    private suspend fun subirAFirebase(
        uri: Uri,
        nombreOriginal: String,
        tipoMime: String
    ): Adjunto {
        val extension = nombreOriginal.substringAfterLast('.', "bin")
        val path = "archivos/${UUID.randomUUID()}.$extension"
        val ref = storage.reference.child(path)

        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        val metadata = ref.metadata.await()

        return Adjunto(
            urlStorage = downloadUrl,
            nombreOriginal = nombreOriginal,
            tipoMime = tipoMime,
            tamanoBytes = metadata.sizeBytes
        )
    }

    suspend fun publicarArchivo(
        uri: Uri,
        nombreOriginal: String,
        tipoMime: String,
        titulo: String,
        descripcion: String,
        tipo: String,
        materiaId: String
    ): Result<FileModel> {
        return try {
            val adjunto = subirAFirebase(uri, nombreOriginal, tipoMime)
            val request = CrearArchivoRequest(
                titulo = titulo,
                descripcion = descripcion,
                tipo = tipo,
                materiaId = materiaId,
                adjuntos = listOf(adjunto)
            )
            val response = filesService.crearArchivo(request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                // CORREGIDO: Accedemos a body?.error?.message en lugar de body?.error
                val errorMsg = body?.error?.message ?: "Error API: ${response.code()} ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPublicados(
        materiaId: String? = null,
        search: String? = null,
        limite: Int? = null,
        offset: Int? = null
    ): Result<List<FileModel>> {
        return try {
            val response = filesService.getArchivosPublicados(materiaId, search, limite, offset)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data.rows)
            } else {
                // CORREGIDO: Accedemos a body?.error?.message en lugar de body?.error
                val errorMsg = body?.error?.message ?: "Error API: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun darLike(archivoId: String): Result<Unit> = try {
        val r = filesService.darLike(archivoId)
        if (r.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun quitarLike(archivoId: String): Result<Unit> = try {
        val r = filesService.quitarLike(archivoId)
        if (r.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun darDislike(archivoId: String): Result<Unit> = try {
        val r = filesService.darDislike(archivoId)
        if (r.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun quitarDislike(archivoId: String): Result<Unit> = try {
        val r = filesService.quitarDislike(archivoId)
        if (r.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    // --- Guardados (favoritos) ---
    suspend fun guardarFavorito(archivoId: String): Result<Unit> = try {
        val r = filesService.guardarFavorito(archivoId)
        if (r.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun quitarFavorito(archivoId: String): Result<Unit> = try {
        val r = filesService.quitarFavorito(archivoId)
        if (r.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun obtenerFavoritos(): Result<List<FileModel>> {
        return try {
            val response = filesService.getArchivosGuardados()
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

    // --- Descargas ---
    // El fileUrl ya viaja en el Post/FileModel (adjuntos.urlStorage), así que aquí solo
    // registramos la descarga como estadística real; abrir el archivo lo hace quien llame esto.
    suspend fun registrarDescarga(archivoId: String): Result<Unit> = try {
        val r = filesService.registrarDescarga(mapOf("archivoId" to archivoId))
        if (r.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    // --- Moderación (admin) ---
    suspend fun obtenerPendientes(): Result<List<FileModel>> {
        return try {
            val response = filesService.getArchivosPendientes()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data.rows)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Error API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aprobarArchivo(archivoId: String): Result<FileModel> =
        actualizarEstado(archivoId, "publicado", null)

    suspend fun rechazarArchivo(archivoId: String, motivo: String): Result<FileModel> =
        actualizarEstado(archivoId, "rechazado", motivo)

    private suspend fun actualizarEstado(archivoId: String, estado: String, motivoRechazo: String?): Result<FileModel> {
        return try {
            val body = mutableMapOf("estado" to estado)
            motivoRechazo?.let { body["motivoRechazo"] = it }

            val response = filesService.actualizarEstadoArchivo(archivoId, body)
            val respBody = response.body()
            if (response.isSuccessful && respBody?.success == true && respBody.data != null) {
                Result.success(respBody.data)
            } else {
                Result.failure(Exception(respBody?.error?.message ?: "Error API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Para archivos de tipo 'url' (GitHub/YouTube): no hay nada que subir a Firebase,
     * solo se manda el enlace tal cual a la API, que valida el dominio. */
    suspend fun publicarEnlace(
        titulo: String,
        descripcion: String,
        url: String,
        materiaId: String
    ): Result<FileModel> {
        return try {
            val adjunto = Adjunto(
                urlStorage = url,
                nombreOriginal = url,
                tipoMime = "text/url",
                tamanoBytes = 0
            )
            val request = CrearArchivoRequest(
                titulo = titulo,
                descripcion = descripcion,
                tipo = "url",
                materiaId = materiaId,
                adjuntos = listOf(adjunto)
            )
            val response = filesService.crearArchivo(request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                val errorMsg = body?.error?.message ?: "Error API: ${response.code()} ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDescargados(): Result<List<FileModel>> {
        return try {
            val response = filesService.getArchivosDescargados()
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

    /** "Mis archivos" para el perfil: todos los que YO subí, sin importar su estado. */
    suspend fun obtenerMisArchivos(): Result<List<FileModel>> {
        return try {
            val response = filesService.getMisArchivos()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(body.data.rows)
            } else {
                Result.failure(Exception(body?.error?.message ?: "Error API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Un solo archivo, con sus contadores y mi like/dislike/guardado reales y actuales
     * (isLikedByMe/isDislikedByMe/isGuardadoByMe), tal como los calcula el backend en
     * este momento. Se usa en FileDetailActivity para no depender de lo que traía el
     * Intent ni de almacenamiento local, que se puede desactualizar o perder. */
    suspend fun obtenerPorId(archivoId: String): Result<FileModel> {
        return try {
            val response = filesService.getArchivoPorId(archivoId)
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
}