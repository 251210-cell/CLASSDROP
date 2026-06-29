package com.classdrop.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityFileRejectedBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.ui.profile.CommunityRulesActivity
import com.classdrop.utils.SessionManager

class FileRejectedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileRejectedBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileRejectedBinding.inflate(layoutInflater)
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

        binding.btnUnderstood.setOnClickListener {
            finish()
        }

        binding.btnReviewRules.setOnClickListener {
            startActivity(Intent(this, CommunityRulesActivity::class.java))
        }
    }
}
