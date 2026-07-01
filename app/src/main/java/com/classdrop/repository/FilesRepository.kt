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
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error API: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}