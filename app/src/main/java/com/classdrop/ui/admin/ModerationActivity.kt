package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityModerationBinding
import com.classdrop.model.ModerationStatus
import com.classdrop.model.ModerationTask
import com.classdrop.utils.SessionManager

class ModerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModerationBinding
    private lateinit var adapter: ModerationAdapter
    private lateinit var sessionManager: SessionManager
    private val taskList = mutableListOf<ModerationTask>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupHeader()
        loadMockTasks()
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

    private fun loadMockTasks() {
        taskList.addAll(listOf(
            ModerationTask(
                "1", "Examen_Parcial_CII_Final.pdf", "Juan Pérez", "Hace 10 min",
                "Patrón de examen institucional detectado. El contenido coincide estructuralmente con evaluaciones previas del departamento de Cálculo."
            ),
            ModerationTask(
                "2", "Solucionario_Guia.pdf", "Juan Pérez", "Hace 10 min",
                "Patrón de examen institucional detectado. El contenido coincide estructuralmente con evaluaciones previas del departamento de Cálculo."
            ),
            ModerationTask(
                "3", "Apunte c++.pdf", "Juan Pérez", "Hace 10 min",
                "Patrón de examen institucional detectado. El contenido coincide estructuralmente con evaluaciones previas del departamento de Cálculo."
            )
        ))
        adapter.submitList(taskList.toList())
    }

    private fun showApprovalDialog(task: ModerationTask) {
        binding.clOverlay.visibility = android.view.View.VISIBLE
        binding.cardConfirm.visibility = android.view.View.VISIBLE
        binding.cardSuccess.visibility = android.view.View.GONE

        binding.tvConfirmTitle.text = "¿Aprobar Archivo?"
        binding.tvConfirmTitle.setTextColor(android.graphics.Color.parseColor("#8B85F5"))
        binding.ivConfirmIcon.setImageResource(com.classdrop.R.drawable.ic_check_circle)
        binding.ivConfirmIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8B85F5"))
        binding.vConfirmIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8B85F5"))
        binding.tvConfirmMessage.text = "¿Deseas validar '${task.fileName}'? El archivo será visible para todos los usuarios."
        binding.btnConfirmAction.text = "Aprobar"
        binding.btnConfirmAction.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6366F1"))

        binding.btnConfirmAction.setOnClickListener {
            processTask(task, ModerationStatus.APPROVED)
            showActionSuccess("Archivo aprobado")
        }

        binding.btnCancelAction.setOnClickListener {
            binding.clOverlay.visibility = android.view.View.GONE
        }
    }

    private fun showRejectionDialog(task: ModerationTask) {
        binding.clOverlay.visibility = android.view.View.VISIBLE
        binding.cardConfirm.visibility = android.view.View.VISIBLE
        binding.cardSuccess.visibility = android.view.View.GONE

        binding.tvConfirmTitle.text = "¿Rechazar Archivo?"
        binding.tvConfirmTitle.setTextColor(android.graphics.Color.parseColor("#EF4444"))
        binding.ivConfirmIcon.setImageResource(com.classdrop.R.drawable.ic_warning)
        binding.ivConfirmIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))
        binding.vConfirmIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))
        binding.tvConfirmMessage.text = "¿Deseas rechazar '${task.fileName}'? Se le notificará al usuario."
        binding.btnConfirmAction.text = "Rechazar"
        binding.btnConfirmAction.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))

        binding.btnConfirmAction.setOnClickListener {
            processTask(task, ModerationStatus.REJECTED)
            showActionSuccess("Archivo rechazado")
        }

        binding.btnCancelAction.setOnClickListener {
            binding.clOverlay.visibility = android.view.View.GONE
        }
    }

    private fun showActionSuccess(message: String) {
        binding.cardConfirm.visibility = android.view.View.GONE
        binding.cardSuccess.visibility = android.view.View.VISIBLE
        binding.tvSuccessTitle.text = message
        
        binding.btnSuccessDone.setOnClickListener {
            binding.clOverlay.visibility = android.view.View.GONE
        }
    }

    private fun processTask(task: ModerationTask, newStatus: ModerationStatus) {
        // En una app real, aquí llamarías a la API
        taskList.remove(task)
        adapter.submitList(taskList.toList())
    }
}
