package com.classdrop.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.classdrop.model.CommentReport
import com.classdrop.model.NotificationType
import com.classdrop.model.ReportStatus

object ReportRepository {
    private val _pendingReports = MutableLiveData<List<CommentReport>>(emptyList())
    val pendingReports: LiveData<List<CommentReport>> = _pendingReports

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

    fun setReports(reports: List<CommentReport>) {
        _pendingReports.value = reports
    }
}
