package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityPrivacyPolicyBinding
import com.classdrop.repository.NormsRepository
import com.classdrop.utils.SessionManager

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var normsRepository: NormsRepository
    private lateinit var adapter: UserNormsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        normsRepository = NormsRepository(this)

        setupHeader()
        setupRecyclerView()
        loadPrivacyData()
        setupListeners()
        
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.tvSupportEmail.setOnClickListener {
            val email = binding.tvSupportEmail.text.toString()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Consulta de Privacidad - ClassDrop")
            }
            try {
                startActivity(Intent.createChooser(intent, "Enviar correo..."))
            } catch (e: Exception) {
                Toast.makeText(this, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = UserNormsAdapter(emptyList())
        binding.rvPrivacyRules.layoutManager = LinearLayoutManager(this)
        binding.rvPrivacyRules.adapter = adapter
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName() ?: "Usuario"
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        
        binding.tvAvatarInitials.text = initials
    }

    private fun loadPrivacyData() {
        binding.tvPrivacyHeaderDesc.text = normsRepository.getPrivacyHeader()
        val rules = normsRepository.getPrivacyRules()
        if (rules.isNotEmpty()) {
            adapter.updateData(rules)
        }
    }
}
