package com.classdrop.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.classdrop.model.ModerationStatus
import com.classdrop.model.ModerationTask
import com.classdrop.model.NotificationType
import android.os.Handler
import android.os.Looper

object ModerationRepository {
    private val _pendingTasks = MutableLiveData<List<ModerationTask>>()
    val pendingTasks: LiveData<List<ModerationTask>> = _pendingTasks

    // Para simular el estado del archivo del usuario actual
    private val _userFileStatus = MutableLiveData<ModerationTask?>()
    val userFileStatus: LiveData<ModerationTask?> = _userFileStatus

    init {
        _pendingTasks.value = listOf(
            ModerationTask("1", "Examen_Parcial_CII_Final.pdf", "Juan Pérez", "Hace 10 min", "Patrón de examen institucional detectado."),
            ModerationTask("2", "Solucionario_Guia.pdf", "Juan Pérez", "Hace 10 min", "Contenido detectado como material de evaluación."),
            ModerationTask("3", "Apunte c++.pdf", "Juan Pérez", "Hace 10 min", "Posible duplicado detectado por IA.")
        )
    }

    fun uploadFile(fileName: String, userName: String) {
        val newTask = ModerationTask(
            id = System.currentTimeMillis().toString(),
            fileName = fileName,
            userName = userName,
            time = "Justo ahora",
            flagReason = "Validando contenido mediante OCR...",
            status = ModerationStatus.PENDING
        )
        
        _userFileStatus.value = newTask

        // Simular el proceso automático de OCR/IA antes de que llegue al admin
        Handler(Looper.getMainLooper()).postDelayed({
            // Después de "Scanning", se añade a la lista del admin
            val currentList = _pendingTasks.value?.toMutableList() ?: mutableListOf()
            currentList.add(newTask)
            _pendingTasks.value = currentList
            
            // NOTIFICAR AL ADMIN: Nuevo archivo requiere revisión
            NotificationRepository.addNotification(
                title = "Nuevo archivo pendiente",
                message = "El usuario $userName ha subido '$fileName'. Requiere revisión manual.",
                type = NotificationType.INFO
            )
        }, 5000)
    }

    fun approveTask(task: ModerationTask) {
        val currentList = _pendingTasks.value?.toMutableList() ?: return
        currentList.remove(task)
        _pendingTasks.value = currentList

        // Si es el archivo del usuario actual, actualizar su estado
        if (_userFileStatus.value?.id == task.id) {
            _userFileStatus.value = task.copy(status = ModerationStatus.APPROVED)
        }
        
        // NOTIFICAR AL USUARIO: Archivo aprobado
        NotificationRepository.addNotification(
            title = "¡Archivo Aprobado!",
            message = "Tu archivo '${task.fileName}' ha sido validado y ya es público.",
            type = NotificationType.SUCCESS
        )
    }

    fun rejectTask(task: ModerationTask) {
        val currentList = _pendingTasks.value?.toMutableList() ?: return
        currentList.remove(task)
        _pendingTasks.value = currentList

        // Si es el archivo del usuario actual, actualizar su estado
        if (_userFileStatus.value?.id == task.id) {
            _userFileStatus.value = task.copy(status = ModerationStatus.REJECTED)
        }
        
        // NOTIFICAR AL USUARIO: Archivo rechazado
        NotificationRepository.addNotification(
            title = "Archivo Rechazado",
            message = "Tu archivo '${task.fileName}' no cumple con las normas de la comunidad.",
            type = NotificationType.ERROR
        )
    }
}
