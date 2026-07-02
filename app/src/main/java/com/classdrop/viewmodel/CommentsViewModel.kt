// app/src/main/java/com/classdrop/viewmodel/CommentsViewModel.kt
package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.Comment
import com.classdrop.network.NetworkResult
import com.classdrop.repository.CommentsRepository
import kotlinx.coroutines.launch

class CommentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CommentsRepository(application)

    private val _commentsState = MutableLiveData<NetworkResult<List<Comment>>>()
    val commentsState: LiveData<NetworkResult<List<Comment>>> = _commentsState

    private val _addCommentState = MutableLiveData<NetworkResult<Comment>>()
    val addCommentState: LiveData<NetworkResult<Comment>> = _addCommentState

    private val _deleteCommentState = MutableLiveData<NetworkResult<String>>()
    val deleteCommentState: LiveData<NetworkResult<String>> = _deleteCommentState

    // Cargar los comentarios de un archivo específico
    fun fetchComments(archivoId: String) {
        _commentsState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _commentsState.value = repository.obtenerComentarios(archivoId)
        }
    }

    // Publicar un nuevo comentario
    fun postComment(archivoId: String, contenido: String) {
        if (contenido.trim().isEmpty()) return

        _addCommentState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _addCommentState.value = repository.publicarComentario(archivoId, contenido)
        }
    }

    // Eliminar un comentario (pasamos el ID para poder removerlo de la lista en la UI)
    fun deleteComment(comentarioId: String) {
        viewModelScope.launch {
            val result = repository.borrarComentario(comentarioId)
            when (result) {
                is NetworkResult.Success -> {
                    _deleteCommentState.value = NetworkResult.Success(comentarioId)
                }
                is NetworkResult.Error -> {
                    _deleteCommentState.value = NetworkResult.Error(result.message ?: "No se pudo eliminar")
                }
                else -> {}
            }
        }
    }

    // Resetear el estado del envío para evitar alertas repetidas al rotar pantalla
    fun resetAddCommentState() {
        _addCommentState.value = null
    }
}