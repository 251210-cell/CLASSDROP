package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.CommunityRule
import com.classdrop.network.NetworkResult
import com.classdrop.repository.PoliticaRepository
import kotlinx.coroutines.launch

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PoliticaRepository(application)

    private val _rulesState = MutableLiveData<NetworkResult<List<CommunityRule>>>()
    val rulesState: LiveData<NetworkResult<List<CommunityRule>>> = _rulesState

    private val _headerState = MutableLiveData<NetworkResult<CommunityRule?>>()
    val headerState: LiveData<NetworkResult<CommunityRule?>> = _headerState

    private val _saveState = MutableLiveData<NetworkResult<CommunityRule>?>()
    val saveState: LiveData<NetworkResult<CommunityRule>?> = _saveState

    private val _deleteState = MutableLiveData<NetworkResult<String>?>()
    val deleteState: LiveData<NetworkResult<String>?> = _deleteState

    /** Carga tanto la lista de reglas como el mensaje principal desde el servidor. */
    fun cargarTodo() {
        cargarReglas()
        cargarMensajePrincipal()
    }

    fun cargarReglas() {
        _rulesState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _rulesState.value = repository.listarReglas()
        }
    }

    fun cargarMensajePrincipal() {
        _headerState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _headerState.value = repository.obtenerMensajePrincipal()
        }
    }

    /** id == null o vacío -> crea una regla nueva; si no, actualiza la existente. */
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

    /** idExistente == null -> aún no existe mensaje principal en el servidor, se crea uno nuevo. */
    fun guardarMensajePrincipal(idExistente: String?, descripcion: String) {
        _saveState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _saveState.value = if (idExistente.isNullOrEmpty()) {
                repository.crearMensajePrincipal(descripcion)
            } else {
                repository.actualizarMensajePrincipal(idExistente, descripcion)
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

    // Para evitar que un mismo resultado se vuelva a procesar al rotar la pantalla, etc.
    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }
}