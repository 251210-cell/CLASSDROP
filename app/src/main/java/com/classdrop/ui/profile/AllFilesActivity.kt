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
        val fileType = intent.getStringExtra("FILE_TYPE") ?: "Archivos"
        
        val adapter = PostsAdapter(sessionManager) { updatedPost ->
            if (fileType == "Favoritos" && !updatedPost.isBookmarked) {
                // Si estamos en la vista de Favoritos y se quita el corazón, eliminamos el item
                val currentList = (binding.rvFiles.adapter as PostsAdapter).currentList.toMutableList()
                currentList.removeAll { it.id == updatedPost.id }
                (binding.rvFiles.adapter as PostsAdapter).submitList(currentList)
            }
        }
        
        binding.rvFiles.layoutManager = LinearLayoutManager(this)
        binding.rvFiles.adapter = adapter

        // Se inicializa con lista vacía para cargar datos reales posteriormente
        adapter.submitList(emptyList<Post>())
    }
}
