package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityAdminProfileBinding
import com.classdrop.ui.auth.LoginActivity
import com.classdrop.utils.SessionManager

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminProfileBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val userName = sessionManager.fetchUserName()
        var userEmail = sessionManager.fetchUserEmail()
        
        // Si el correo está vacío, usamos el predeterminado de administración
        if (userEmail.isBlank()) {
            userEmail = "admin.classdrop@gmail.com"
        }

        binding.tvAdminName.text = userName
        binding.tvAdminEmail.text = userEmail
        binding.tvDetailName.text = userName
        binding.tvDetailEmail.text = userEmail

        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        
        binding.tvAvatarLarge.text = initials
        
        // Mock stats for display
        binding.tvModeratedCount.text = "156"
        binding.tvReportsResolved.text = "42"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutOverlay()
        }

        binding.btnCancelLogout.setOnClickListener {
            hideLogoutOverlay()
        }

        binding.btnConfirmLogout.setOnClickListener {
            logout()
        }
    }

    private fun showLogoutOverlay() {
        binding.clOverlay.visibility = View.VISIBLE
    }

    private fun hideLogoutOverlay() {
        binding.clOverlay.visibility = View.GONE
    }

    private fun logout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}
