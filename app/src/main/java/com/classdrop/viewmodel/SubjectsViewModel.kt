package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.CuatrimestreResponse
import com.classdrop.model.MateriaResponse
import com.classdrop.repository.SubjectsRepository
import kotlinx.coroutines.launch

class SubjectsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubjectsRepository(application)

    private val _materias = MutableLiveData<List<MateriaResponse>>()
    val materias: LiveData<List<MateriaResponse>> get() = _materias

    private val _cuatrimestres = MutableLiveData<List<CuatrimestreResponse>>()
    val cuatrimestres: LiveData<List<CuatrimestreResponse>> get() = _cuatrimestres

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Cargar materias generales
    fun fetchAllMaterias(search: String? = null) {
        viewModelScope.launch {
            try {
                val response = repository.getAllMaterias(search)
                if (response.isSuccessful) {
                    _materias.postValue(response.body() ?: emptyList())
                } else {
                    _error.postValue("Error al cargar materias: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error de red: ${e.message}")
            }
        }
    }

    // Cargar la lista de cuatrimestres
    fun fetchCuatrimestres() {
        viewModelScope.launch {
            try {
                val response = repository.getCuatrimestres()
                if (response.isSuccessful) {
                    _cuatrimestres.postValue(response.body() ?: emptyList())
                } else {
                    _error.postValue("Error al cargar cuatrimestres: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error de conexión: ${e.message}")
            }
        }
    }

    // Buscar una materia puntual, útil para precargar el formulario de edición
    fun obtenerMateriaPorId(id: String): MateriaResponse? {
        return _materias.value?.find { it.id == id }
    }

    fun cargarMateriaPorId(id: String, onResult: (MateriaResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.getMateriaById(id)
                onResult(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                _error.postValue("Error de red: ${e.message}")
                onResult(null)
            }
        }
    }

    fun crearMateria(
        nombre: String,
        cuatrimestreId: Int,
        icono: String?,
        onResult: (exito: Boolean, mensajeError: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.createMateria(nombre, cuatrimestreId, icono)
                if (response.isSuccessful) {
                    fetchAllMaterias() // refresca la lista con la materia recién creada
                    onResult(true, null)
                } else {
                    val cuerpoError = response.errorBody()?.string()
                    onResult(false, "Error del servidor (${response.code()}): $cuerpoError")
                }
            } catch (e: Exception) {
                onResult(false, "Error de red: ${e.message}")
            }
        }
    }

    fun actualizarMateria(
        id: String,
        campos: Map<String, Any>,
        onResult: (exito: Boolean, mensajeError: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updateMateria(id, campos)
                if (response.isSuccessful) {
                    fetchAllMaterias()
                    onResult(true, null)
                } else {
                    onResult(false, "Error del servidor (${response.code()})")
                }
            } catch (e: Exception) {
                onResult(false, "Error de red: ${e.message}")
            }
        }
    }

    fun deleteSubject(subject: MateriaResponse, onResult: (exito: Boolean, mensajeError: String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                val response = repository.deleteMateria(subject.id)
                if (response.isSuccessful) {
                    // Quitamos la materia de la lista en memoria sin esperar otro round-trip
                    _materias.value = _materias.value?.filterNot { it.id == subject.id }
                    onResult(true, null)
                } else {
                    onResult(false, "Error del servidor (${response.code()})")
                }
            } catch (e: Exception) {
                onResult(false, "Error de red: ${e.message}")
            }
        }
    }
}