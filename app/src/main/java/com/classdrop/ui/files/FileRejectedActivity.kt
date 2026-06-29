package com.classdrop.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityFileRejectedBinding
import com.classdrop.ui.profile.CommunityRulesActivity

class FileRejectedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileRejectedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileRejectedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
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
