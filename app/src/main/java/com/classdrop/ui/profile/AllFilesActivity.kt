package com.classdrop.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityAllFilesBinding
import com.classdrop.ui.explore.Post
import com.classdrop.ui.explore.PostsAdapter
import com.classdrop.utils.SessionManager

class AllFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllFilesBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        
        val type = intent.getStringExtra("FILE_TYPE") ?: "Archivos"
        binding.tvTitle.text = type

        setupHeader()
        setupRecyclerView()
        
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        binding.tvAvatarInitials.text = initials
    }

    private fun setupRecyclerView() {
        val adapter = PostsAdapter()
        binding.rvFiles.layoutManager = LinearLayoutManager(this)
        binding.rvFiles.adapter = adapter

        // Mock data based on the type
        val mockData = when(intent.getStringExtra("FILE_TYPE")) {
            "Favoritos" -> listOf(
                Post(id = "1", userName = "Elena García", time = "hace 2 horas", fileName = "Cálculo Vectorial", fileType = "PDF", likes = 42, downloads = 128, comments = 8),
                Post(id = "2", userName = "Marcos Ruiz", time = "hace 5 horas", fileName = "Estructuras de Datos", fileType = "PDF", likes = 15, downloads = 56, comments = 5)
            )
            "Descargas" -> listOf(
                Post(id = "1", userName = "Sistema", time = "ayer", fileName = "Guía de Estudio Redes", fileType = "PDF", likes = 100, downloads = 250, comments = 20)
            )
            else -> listOf(
                Post(id = "1", userName = "Yo", time = "hace 2 días", fileName = "Derivadas Parciales", fileType = "PDF", likes = 10, downloads = 30, comments = 2),
                Post(id = "2", userName = "Yo", time = "hace 5 días", fileName = "Proyecto CRUD", fileType = "URL", likes = 5, downloads = 20, comments = 1),
                Post(id = "3", userName = "Yo", time = "hace 1 semana", fileName = "Álgebra Lineal — resumen", fileType = "PDF", likes = 8, downloads = 15, comments = 0)
            )
        }
        adapter.submitList(mockData)
    }
}
