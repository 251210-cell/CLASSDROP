package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityPrivacyPolicyBinding
import com.classdrop.repository.NormsRepository
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.PrivacyViewModel

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var normsRepository: NormsRepository
    private lateinit var adapter: UserNormsAdapter
    private val viewModel: PrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        normsRepository = NormsRepository(this)

        setupHeader()
        setupRecyclerView()
        setupViewModel()
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
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Error",
                    message = "No se encontró una aplicación de correo en este dispositivo",
                    type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                )
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

    private fun setupViewModel() {
        // Observar el encabezado de privacidad
        binding.tvPrivacyHeaderDesc.text = normsRepository.getPrivacyHeader()

        // Observar las reglas de privacidad desde el ViewModel (Room Database)
        // Esto asegura que los cambios hechos por el admin se reflejen aquí
        viewModel.privacyRules.observe(this) { rules ->
            if (rules.isNotEmpty()) {
                adapter.updateData(rules)
            }
        }
    }
}
