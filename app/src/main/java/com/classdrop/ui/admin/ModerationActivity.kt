package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityModerationBinding
import com.classdrop.model.ModerationStatus
import com.classdrop.model.ModerationTask
import com.classdrop.model.NotificationType
import com.classdrop.repository.ModerationRepository
import com.classdrop.repository.NotificationRepository
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager

class ModerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModerationBinding
    private lateinit var adapter: ModerationAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupHeader()
        observeTasks()
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

        adapter = ModerationAdapter(
            onApprove = { task -> showApprovalDialog(task) },
            onReject = { task -> showRejectionDialog(task) }
        )

        binding.rvModeration.apply {
            layoutManager = LinearLayoutManager(this@ModerationActivity)
            adapter = this@ModerationActivity.adapter
        }
    }

    private fun observeTasks() {
        ModerationRepository.pendingTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
        }
    }

    private fun showApprovalDialog(task: ModerationTask) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Aprobar Archivo?",
            message = "¿Deseas validar '${task.fileName}'? El archivo será visible para todos los usuarios.",
            type = AlertUtils.AlertType.CONFIRMATION,
            primaryButtonText = "Aprobar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                ModerationRepository.approveTask(task)
                showActionSuccess("Archivo aprobado")
            }
        )
    }

    private fun showRejectionDialog(task: ModerationTask) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Rechazar Archivo?",
            message = "¿Deseas rechazar '${task.fileName}'? Se le notificará al usuario.",
            type = AlertUtils.AlertType.ERROR,
            primaryButtonText = "Rechazar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                ModerationRepository.rejectTask(task)
                showActionSuccess("Archivo rechazado")
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
