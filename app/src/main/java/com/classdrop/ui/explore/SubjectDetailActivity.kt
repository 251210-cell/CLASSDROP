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
            Post("1", "Elena García", "hace 2 horas", "Resumen Integrales Triples", "PDF", 42, 128, 8),
            Post("2", "Marcos Ruiz", "hace 5 horas", "Guía Práctica Derivadas", "DOCX", 15, 56, 5),
            Post("3", "Sofia Alva", "ayer", "Guía de Estudio Final", "PDF", 89, 210, 12)
        )
        adapter.submitList(mockPosts)
    }
}