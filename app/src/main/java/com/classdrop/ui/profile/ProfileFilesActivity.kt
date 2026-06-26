package com.classdrop.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityProfileFilesBinding

class ProfileFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileFilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("FILE_TYPE") ?: "UPLOADS"
        setupUI(type)
    }

    private fun setupUI(type: String) {
        binding.tvToolbarTitle.text = if (type == "UPLOADS") "Mis Archivos Subidos" else "Mis Favoritos"
        
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Aquí se configuraría el RecyclerView con el adaptador correspondiente
        // Por ahora es un contenedor para la funcionalidad visual
    }
}