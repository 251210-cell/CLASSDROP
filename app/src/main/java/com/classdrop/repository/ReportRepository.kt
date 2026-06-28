package com.classdrop.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.classdrop.model.CommentReport
import com.classdrop.model.NotificationType
import com.classdrop.model.ReportStatus

object ReportRepository {
    private val _pendingReports = MutableLiveData<List<CommentReport>>()
    val pendingReports: LiveData<List<CommentReport>> = _pendingReports

    init {
        _pendingReports.value = listOf(
            CommentReport(
                "1", "Mateo G.", "Lucas R.", "hace 2 horas", "5/5",
                "Dudas Práctica 3 - Cálculo II",
                "\"No sabes nada porque preguntas eso tramita tu baja mejor '.\""
            ),
            CommentReport(
                "2", "Sofía L.", "Anon123", "hace 5 horas", "5/5",
                "Resumen Física I - Cinemática",
                "\"GANA CRIPTO FACIL ENTRA A ESTE LINK http://spamlink.com/cripto YA MISMO!!\""
            )
        )
    }

    fun keepComment(report: CommentReport) {
        val currentList = _pendingReports.value?.toMutableList() ?: return
        currentList.remove(report)
        _pendingReports.value = currentList
        
        // Notificación opcional al reportero: "Gracias por tu reporte, el contenido fue validado."
    }

    fun removeComment(report: CommentReport) {
        val currentList = _pendingReports.value?.toMutableList() ?: return
        currentList.remove(report)
        _pendingReports.value = currentList

        // ENVIAR NOTIFICACIÓN REAL AL USUARIO REPORTADO
        NotificationRepository.addNotification(
            title = "Comentario Eliminado",
            message = "Tu comentario en '${report.contextTitle}' fue eliminado por no cumplir con las normas de convivencia académica.",
            type = NotificationType.ERROR
        )
    }
}
