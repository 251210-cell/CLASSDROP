package com.classdrop.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityFileSuccessBinding
import com.classdrop.ui.main.MainActivity

class FileSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnViewPublication.setOnClickListener {
            // Ir a la vista de detalles del archivo o al perfil
            finish()
        }

        binding.btnUploadAnother.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("NAVIGATE_TO", "UPLOAD")
            }
            startActivity(intent)
            finish()
        }
    }
}
