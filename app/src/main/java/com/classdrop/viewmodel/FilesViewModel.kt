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