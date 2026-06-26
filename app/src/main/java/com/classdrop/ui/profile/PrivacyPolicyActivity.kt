package com.classdrop.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupBottomNav()
    }

    private fun setupBottomNav() {
        // En esta vista, como es de información, los botones del nav simplemente vuelven al main o cierran
        val nav = binding.includeBottomNav
        nav.btnNavHome.setOnClickListener { finish() }
        nav.btnNavSearch.setOnClickListener { finish() }
        nav.btnNavNotes.setOnClickListener { finish() }
        nav.btnNavUpload.setOnClickListener { finish() }
    }
}