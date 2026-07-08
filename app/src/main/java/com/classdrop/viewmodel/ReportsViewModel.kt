package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.Reporte
import com.classdrop.repository.ReportRepository
import kotlinx.coroutines.launch

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReportRepository(application)

    private val _pendientes = MutableLiveData<List<Reporte>>(emptyList())
    val pendientes: LiveData<List<Reporte>> = _pendientes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMensaje = MutableLiveData<String?>()
    val errorMensaje: LiveData<String?> = _errorMensaje

    // Se dispara cuando una acción (mantener/eliminar) se resuelve bien, para
    // que la Activity pueda mostrar el diálogo de éxito correspondiente.
    private val _accionExitosa = MutableLiveData<String?>()
    val accionExitosa: LiveData<String?> = _accionExitosa

    fun cargarPendientes() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.obtenerPendientes().fold(
                onSuccess = {
                    _pendientes.value = it
                    _isLoading.value = false
                },
                onFailure = {
                    _errorMensaje.value = it.message ?: "No se pudieron cargar los reportes"
                    _isLoading.value = false
                }
            )
        }
    }

    /** Visto bueno: el contenido no tenía ningún problema, se restaura. */
    fun mantener(reporte: Reporte) {
        resolver(reporte, "descartado", "Contenido validado por un administrador", "Comentario mantenido")
    }

    /** Visto malo: se confirma la falta, el contenido se borra definitivamente. */
    fun eliminar(reporte: Reporte) {
        resolver(reporte, "resuelto", "Contenido eliminado por incumplir las normas", "Contenido eliminado")
    }

    private fun resolver(reporte: Reporte, estado: String, accionTomada: String, mensajeExito: String) {
        viewModelScope.launch {
            repository.resolver(reporte.id, estado, accionTomada).fold(
                onSuccess = {
                    // Lo quitamos de la lista local al instante, sin esperar
                    // un refetch completo.
                    _pendientes.value = _pendientes.value?.filterNot { it.id == reporte.id }
                    _accionExitosa.value = mensajeExito
                },
                onFailure = {
                    _errorMensaje.value = it.message ?: "No se pudo completar la acción"
                }
            )
        }
    }

    fun limpiarMensajes() {
        _errorMensaje.value = null
        _accionExitosa.value = null
    }
}