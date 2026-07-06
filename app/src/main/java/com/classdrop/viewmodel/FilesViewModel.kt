package com.classdrop.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.FileModel
import com.classdrop.network.NetworkResult
import com.classdrop.repository.FilesRepository
import kotlinx.coroutines.launch

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val file: FileModel) : UploadState()
    data class Error(val message: String) : UploadState()
}

class FilesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FilesRepository(application)

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

    // --- Guardados (favoritos) ---
    fun actualizarFavorito(archivoId: String, isActivo: Boolean) {
        viewModelScope.launch {
            val result = if (isActivo) repository.guardarFavorito(archivoId) else repository.quitarFavorito(archivoId)
            result.onFailure { _listError.value = it.message }
        }
    }

    // --- Descargas ---
    // El fileUrl real ya viaja en el Post (adjuntos.urlStorage vía backend), así que aquí solo
    // registramos la descarga como estadística; abrir el archivo lo hace quien tenga la URL.
    fun registrarDescarga(archivoId: String) {
        viewModelScope.launch {
            repository.registrarDescarga(archivoId)
        }
    }

    fun publicarEnlace(titulo: String, descripcion: String, url: String, materiaId: String) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            val result = repository.publicarEnlace(titulo, descripcion, url, materiaId)
            _uploadState.value = result.fold(
                onSuccess = { UploadState.Success(it) },
                onFailure = { UploadState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    // --- Moderación (admin) ---
    private val _pendientes = MutableLiveData<List<FileModel>>()
    val pendientes: LiveData<List<FileModel>> = _pendientes

    fun cargarPendientes() {
        viewModelScope.launch {
            val result = repository.obtenerPendientes()
            result.fold(
                onSuccess = {
                    _listError.value = null
                    _pendientes.value = it
                },
                onFailure = { _listError.value = it.message }
            )
        }
    }

    fun aprobarArchivo(archivoId: String, onResult: (exito: Boolean, mensajeError: String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.aprobarArchivo(archivoId)
            result.fold(
                onSuccess = {
                    _pendientes.value = _pendientes.value?.filterNot { it.id == archivoId }
                    onResult(true, null)
                },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun rechazarArchivo(archivoId: String, motivo: String, onResult: (exito: Boolean, mensajeError: String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.rechazarArchivo(archivoId, motivo)
            result.fold(
                onSuccess = {
                    _pendientes.value = _pendientes.value?.filterNot { it.id == archivoId }
                    onResult(true, null)
                },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    // --- Perfil: mis archivos / descargados / favoritos ---
    private val _misArchivos = MutableLiveData<List<FileModel>>()
    val misArchivos: LiveData<List<FileModel>> = _misArchivos

    private val _descargados = MutableLiveData<List<FileModel>>()
    val descargados: LiveData<List<FileModel>> = _descargados

    private val _favoritos = MutableLiveData<List<FileModel>>()
    val favoritos: LiveData<List<FileModel>> = _favoritos

    fun cargarMisArchivos() {
        viewModelScope.launch {
            repository.obtenerMisArchivos().fold(
                onSuccess = { _misArchivos.value = it },
                onFailure = { _listError.value = it.message }
            )
        }
    }

    // --- Detalle de un archivo (FileDetailActivity) ---
    private val _archivoDetalle = MutableLiveData<NetworkResult<FileModel>>()
    val archivoDetalle: LiveData<NetworkResult<FileModel>> = _archivoDetalle

    fun cargarArchivo(archivoId: String) {
        _archivoDetalle.value = NetworkResult.Loading()
        viewModelScope.launch {
            repository.obtenerPorId(archivoId).fold(
                onSuccess = { _archivoDetalle.value = NetworkResult.Success(it) },
                onFailure = { _archivoDetalle.value = NetworkResult.Error(it.message ?: "No se pudo cargar el archivo") }
            )
        }
    }

    fun cargarDescargados() {
        viewModelScope.launch {
            repository.obtenerDescargados().fold(
                onSuccess = { _descargados.value = it },
                onFailure = { _listError.value = it.message }
            )
        }
    }

    fun cargarFavoritos() {
        viewModelScope.launch {
            repository.obtenerFavoritos().fold(
                onSuccess = { _favoritos.value = it },
                onFailure = { _listError.value = it.message }
            )
        }
    }
}