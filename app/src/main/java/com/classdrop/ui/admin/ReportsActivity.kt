package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityReportsBinding
import com.classdrop.model.CommentReport
import com.classdrop.repository.ReportRepository
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
        binding.clOverlay.visibility = android.view.View.VISIBLE
        binding.cardConfirm.visibility = android.view.View.VISIBLE
        binding.cardSuccess.visibility = android.view.View.GONE

        binding.tvConfirmTitle.text = "¿Mantener Comentario?"
        binding.tvConfirmTitle.setTextColor(android.graphics.Color.parseColor("#8B85F5"))
        binding.ivConfirmIcon.setImageResource(com.classdrop.R.drawable.ic_check_circle)
        binding.ivConfirmIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8B85F5"))
        binding.vConfirmIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8B85F5"))
        binding.tvConfirmMessage.text = "¿Estás seguro de que este comentario es apto para la plataforma?"
        binding.btnConfirmAction.text = "Mantener"
        binding.btnConfirmAction.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6366F1"))

        binding.btnConfirmAction.setOnClickListener {
            com.classdrop.repository.ReportRepository.keepComment(report)
            binding.cardConfirm.visibility = android.view.View.GONE
            binding.tvSuccessTitle.text = "Comentario mantenido"
            binding.cardSuccess.visibility = android.view.View.VISIBLE
            
            binding.btnSuccessDone.setOnClickListener {
                binding.clOverlay.visibility = android.view.View.GONE
            }
        }

        binding.btnCancelAction.setOnClickListener {
            binding.clOverlay.visibility = android.view.View.GONE
        }
    }

    private fun showRemoveConfirmation(report: CommentReport) {
        binding.clOverlay.visibility = android.view.View.VISIBLE
        binding.cardConfirm.visibility = android.view.View.VISIBLE
        binding.cardSuccess.visibility = android.view.View.GONE

        binding.tvConfirmTitle.text = "¿Eliminar Comentario?"
        binding.tvConfirmTitle.setTextColor(android.graphics.Color.parseColor("#EF4444"))
        binding.ivConfirmIcon.setImageResource(com.classdrop.R.drawable.ic_delete)
        binding.ivConfirmIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))
        binding.vConfirmIconBg.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))
        binding.tvConfirmMessage.text = "¿Deseas eliminar este comentario? Se le notificará al usuario que su contenido no es apto."
        binding.btnConfirmAction.text = "Eliminar"
        binding.btnConfirmAction.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))

        binding.btnConfirmAction.setOnClickListener {
            com.classdrop.repository.ReportRepository.removeComment(report)
            binding.cardConfirm.visibility = android.view.View.GONE
            binding.tvSuccessTitle.text = "Comentario eliminado"
            binding.cardSuccess.visibility = android.view.View.VISIBLE
            
            binding.btnSuccessDone.setOnClickListener {
                binding.clOverlay.visibility = android.view.View.GONE
            }
        }

        binding.btnCancelAction.setOnClickListener {
            binding.clOverlay.visibility = android.view.View.GONE
        }
    }
}
