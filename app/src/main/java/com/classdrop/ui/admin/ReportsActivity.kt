package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityReportsBinding
import com.classdrop.model.CommentReport
import com.classdrop.repository.ReportRepository
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var adapter: ReportsAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupHeader()
        observeReports()
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName() ?: "Admin"
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        
        binding.tvAvatarInitials.text = initials
        binding.tvAvatarInitials.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }
        
        binding.ivNotificationAdmin.setOnClickListener {
            startActivity(Intent(this, com.classdrop.ui.notifications.NotificationsActivity::class.java))
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = ReportsAdapter(
            onKeep = { report -> showKeepConfirmation(report) },
            onRemove = { report -> showRemoveConfirmation(report) }
        )

        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(this@ReportsActivity)
            adapter = this@ReportsActivity.adapter
        }
    }

    private fun observeReports() {
        ReportRepository.pendingReports.observe(this) { reports ->
            adapter.submitList(reports)
        }
    }

    private fun showKeepConfirmation(report: CommentReport) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Mantener Comentario?",
            message = "¿Estás seguro de que este comentario es apto para la plataforma?",
            type = AlertUtils.AlertType.CONFIRMATION,
            primaryButtonText = "Mantener",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                com.classdrop.repository.ReportRepository.keepComment(report)
                showActionSuccess("Comentario mantenido")
            }
        )
    }

    private fun showRemoveConfirmation(report: CommentReport) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Eliminar Comentario?",
            message = "¿Deseas eliminar este comentario? Se le notificará al usuario que su contenido no es apto.",
            type = AlertUtils.AlertType.ERROR,
            primaryButtonText = "Eliminar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                com.classdrop.repository.ReportRepository.removeComment(report)
                showActionSuccess("Comentario eliminado")
            }
        )
    }

    private fun showActionSuccess(message: String) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "Éxito",
            message = message,
            type = AlertUtils.AlertType.SUCCESS,
            primaryButtonText = "Entendido"
        )
    }
}
