package com.classdrop.ui.explore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivitySubjectDetailBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager

class SubjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubjectDetailBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Materia"
        val fileCount = intent.getIntExtra("FILE_COUNT", 0)
        
        binding.tvSubjectTitle.text = subjectName
        binding.tvSubtitle.text = "$fileCount archivos compartidos"

        binding.btnBack.setOnClickListener {
            finish()
        }
        
        setupHeader()
        setupPosts()
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        
        binding.tvAvatarInitials.text = initials
        
        // Al hacer clic en el avatar, ir al perfil (en MainActivity)
        binding.tvAvatarInitials.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SELECT_TAB", "PROFILE")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupPosts() {
        val adapter = PostsAdapter()
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter

        val mockPosts = listOf(
            Post(
                id = "1",
                userName = "Elena García",
                time = "hace 30 min • Cálculo II",
                fileName = "Resumen: Derivadas Parciales v2",
                fileType = "PDF",
                likes = 125,
                dislikes = 45,
                downloads = 128,
                comments = 8,
                isLiked = true,
                isBookmarked = true
            ),
            Post(
                id = "2",
                userName = "Marco Soto",
                time = "ayer • Programación",
                fileName = "Guía Práctica Derivadas",
                fileType = "DOCX",
                likes = 15,
                dislikes = 2,
                downloads = 56,
                comments = 5
            ),
            Post(
                id = "3",
                userName = "Sofia Alva",
                time = "ayer • Cálculo II",
                fileName = "Guía de Estudio Final",
                fileType = "PDF",
                likes = 89,
                dislikes = 10,
                downloads = 210,
                comments = 12
            )
        )
        adapter.submitList(mockPosts)
    }
}