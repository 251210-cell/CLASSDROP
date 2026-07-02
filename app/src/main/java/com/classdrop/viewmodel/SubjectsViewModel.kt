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
}