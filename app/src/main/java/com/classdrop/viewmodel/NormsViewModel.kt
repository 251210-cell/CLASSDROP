package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.CommunityRule
import com.classdrop.network.NetworkResult
import com.classdrop.repository.NormaRepository
import kotlinx.coroutines.launch

class NormsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NormaRepository(application)

    private val _rulesState = MutableLiveData<NetworkResult<List<CommunityRule>>>()
    val rulesState: LiveData<NetworkResult<List<CommunityRule>>> = _rulesState

    private val _sanctionsState = MutableLiveData<NetworkResult<CommunityRule?>>()
    val sanctionsState: LiveData<NetworkResult<CommunityRule?>> = _sanctionsState

    private val _saveState = MutableLiveData<NetworkResult<CommunityRule>?>()
    val saveState: LiveData<NetworkResult<CommunityRule>?> = _saveState

    private val _deleteState = MutableLiveData<NetworkResult<String>?>()
    val deleteState: LiveData<NetworkResult<String>?> = _deleteState

    /** Carga tanto la lista de normas como el régimen sancionatorio desde el servidor. */
    fun cargarTodo() {
        cargarReglas()
        cargarSanciones()
    }

    fun cargarReglas() {
        _rulesState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _rulesState.value = repository.listarReglas()
        }
    }

    fun cargarSanciones() {
        _sanctionsState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _sanctionsState.value = repository.obtenerSanciones()
        }
    }

    /** id == null o vacío -> crea una norma nueva; si no, actualiza la existente. */
    fun guardarRegla(id: String?, titulo: String, descripcion: String) {
        _saveState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _saveState.value = if (id.isNullOrEmpty()) {
                repository.crearRegla(titulo, descripcion)
            } else {
                repository.actualizarRegla(id, titulo, descripcion)
            }
        }
    }

    /** idExistente == null -> aún no existe el régimen sancionatorio en el servidor, se crea uno nuevo. */
    fun guardarSanciones(idExistente: String?, descripcion: String) {
        _saveState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _saveState.value = if (idExistente.isNullOrEmpty()) {
                repository.crearSanciones(descripcion)
            } else {
                repository.actualizarSanciones(idExistente, descripcion)
            }
        }
    }

    fun eliminarRegla(id: String) {
        viewModelScope.launch {
            when (val result = repository.eliminarRegla(id)) {
                is NetworkResult.Success -> _deleteState.value = NetworkResult.Success(id)
                is NetworkResult.Error -> _deleteState.value = NetworkResult.Error(result.message ?: "No se pudo eliminar")
                else -> {}
            }
        }
    }

    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }
}