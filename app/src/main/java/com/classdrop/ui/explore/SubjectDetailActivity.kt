package com.classdrop.ui.explore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivitySubjectDetailBinding

class SubjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubjectDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Materia"
        val fileCount = intent.getIntExtra("FILE_COUNT", 0)
        
        binding.tvSubjectTitle.text = subjectName
        binding.tvSubtitle.text = "$fileCount archivos compartidos"

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupPosts()
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