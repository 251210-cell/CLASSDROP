package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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

        binding.cardSupport.setOnClickListener {
            sendEmail()
        }

        setupBottomNav()
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte.classdrop@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Consulta Políticas de Privacidad - ClassDrop")
        }
        try {
            startActivity(Intent.createChooser(intent, "Enviar correo con..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav() {
        val nav = binding.includeBottomNav
        nav.btnNavHome.setOnClickListener { finish() }
        nav.btnNavSearch.setOnClickListener { finish() }
        nav.btnNavNotes.setOnClickListener { finish() }
        nav.btnNavUpload.setOnClickListener { finish() }
    }
}