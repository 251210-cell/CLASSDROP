package com.classdrop.repository

import android.content.Context
import android.net.Uri
import com.classdrop.model.Adjunto
import com.classdrop.model.CrearArchivoRequest
import com.classdrop.model.FileModel
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
                Result.failure(Exception(body?.error ?: "Error API: ${response.code()} ${response.message()}"))
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
                Result.failure(Exception(body?.error ?: "Error API: ${response.code()}"))
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
}