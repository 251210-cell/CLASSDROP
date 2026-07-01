package com.classdrop.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.Comment
import com.classdrop.model.FileModel
import com.classdrop.repository.FilesRepository
import kotlinx.coroutines.launch
import java.util.UUID

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val file: FileModel) : UploadState()
    data class Error(val message: String) : UploadState()
}

class FilesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FilesRepository(application)

    // --- Comentarios (igual que antes, lo usa FileDetailActivity) ---
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    init {
        loadMockComments()
    }

    private fun loadMockComments() {
        val mockComments = listOf(
            Comment("1", "user1", "¡Excelente material! Me sirvió mucho para el examen.", System.currentTimeMillis() - 3600000),
            Comment("2", "user2", "Faltan un par de fórmulas en la segunda página, pero está muy completo.", System.currentTimeMillis() - 7200000)
        )
        _comments.value = mockComments
    }

    fun addComment(content: String) {
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            userId = "currentUser",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val currentList = _comments.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, newComment)
        _comments.value = currentList
    }

    // --- Subida de archivos ---
    private val _uploadState = MutableLiveData<UploadState>(UploadState.Idle)
    val uploadState: LiveData<UploadState> = _uploadState

    // --- Listado real de archivos publicados ---
    private val _archivosPublicados = MutableLiveData<List<FileModel>>()
    val archivosPublicados: LiveData<List<FileModel>> = _archivosPublicados

    private val _listError = MutableLiveData<String?>()
    val listError: LiveData<String?> = _listError

    fun cargarArchivosPublicados(materiaId: String? = null, search: String? = null) {
        viewModelScope.launch {
            val result = repository.obtenerPublicados(materiaId = materiaId, search = search)
            result.fold(
                onSuccess = {
                    _listError.value = null
                    _archivosPublicados.value = it
                },
                onFailure = { _listError.value = it.message }
            )
        }
    }

    // isActivo = true significa "el usuario acaba de activar" (ya se refleja optimistamente en la UI);
    // si falla la llamada al backend, no revertimos la UI para no complicar el ejemplo, pero queda
    // el error disponible en listError para mostrarlo si quieres agregar esa lógica después.
    fun actualizarLike(archivoId: String, isActivo: Boolean) {
        viewModelScope.launch {
            val result = if (isActivo) repository.darLike(archivoId) else repository.quitarLike(archivoId)
            result.onFailure { _listError.value = it.message }
        }
    }

    fun actualizarDislike(archivoId: String, isActivo: Boolean) {
        viewModelScope.launch {
            val result = if (isActivo) repository.darDislike(archivoId) else repository.quitarDislike(archivoId)
            result.onFailure { _listError.value = it.message }
        }
    }

    fun publicarArchivo(
        uri: Uri, nombreOriginal: String, tipoMime: String,
        titulo: String, descripcion: String, tipo: String, materiaId: String
    ) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            val result = repository.publicarArchivo(
                uri, nombreOriginal, tipoMime, titulo, descripcion, tipo, materiaId
            )
            _uploadState.value = result.fold(
                onSuccess = { UploadState.Success(it) },
                onFailure = { UploadState.Error(it.message ?: "Error desconocido") }
            )
        }
    }
}