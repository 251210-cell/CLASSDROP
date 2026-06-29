package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityCommunityRulesBinding
import com.classdrop.repository.NormsRepository
import com.classdrop.utils.SessionManager
import androidx.recyclerview.widget.LinearLayoutManager

class CommunityRulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityRulesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var normsRepository: NormsRepository
    private lateinit var adapter: UserNormsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        normsRepository = NormsRepository(this)

        setupHeader()
        setupRecyclerView()
        loadData()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = UserNormsAdapter(emptyList())
        binding.rvUserNorms.layoutManager = LinearLayoutManager(this)
        binding.rvUserNorms.adapter = adapter
    }

    private fun loadData() {
        val rules = normsRepository.getRules()
        if (rules.isNotEmpty()) {
            adapter.updateData(rules)
        }
        binding.tvSanctionsDescription.text = normsRepository.getSanctions()
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

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnContactSupport.setOnClickListener {
            sendEmail()
        }

        // Configurar navegación del bottom nav para volver al main
        val nav = binding.includeBottomNav
        nav.btnNavHome.setOnClickListener { finish() }
        nav.btnNavSearch.setOnClickListener { finish() }
        nav.btnNavNotes.setOnClickListener { finish() }
        nav.btnNavUpload.setOnClickListener { finish() }
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte.classdrop@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Consulta Reglamento de la Comunidad - ClassDrop")
        }
        try {
            startActivity(Intent.createChooser(intent, "Enviar correo con..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
        }
    }
}