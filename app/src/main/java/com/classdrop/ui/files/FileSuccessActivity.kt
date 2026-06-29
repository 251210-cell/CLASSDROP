package com.classdrop.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityFileSuccessBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager

class FileSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileSuccessBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupHeader()
        setupListeners()
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        binding.tvAvatarInitials.text = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        binding.tvAvatarInitials.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("SELECT_TAB", "PROFILE")
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnViewPublication.setOnClickListener {
            // Ir al perfil para ver la publicación
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("SELECT_TAB", "PROFILE")
            }
            startActivity(intent)
            finish()
        }

        binding.btnUploadAnother.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("SELECT_TAB", "HOME") // O una pestaña de subida si existiera
            }
            startActivity(intent)
            finish()
        }
    }
}
