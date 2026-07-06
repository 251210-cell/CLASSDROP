package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityPrivacyPolicyBinding
import com.classdrop.network.NetworkResult
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.PrivacyViewModel

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: UserNormsAdapter
    private val viewModel: PrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupHeader()
        setupRecyclerView()
        setupViewModel()
        setupListeners()

        // Trae siempre la versión más reciente publicada por el admin.
        viewModel.cargarTodo()

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
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "Error",
                    message = "No se encontró una aplicación de correo en este dispositivo",
                    type = AlertUtils.AlertType.ERROR
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
        // Mensaje principal, tal como lo dejó el admin en el servidor
        viewModel.headerState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.tvPrivacyHeaderDesc.text = result.data?.description
                        ?: "Aún no se ha publicado un mensaje principal de privacidad."
                }
                is NetworkResult.Error -> {
                    binding.tvPrivacyHeaderDesc.text = "No se pudo cargar el mensaje principal."
                }
                else -> {}
            }
        }

        // Lista de políticas de privacidad, igual para todos los estudiantes
        viewModel.rulesState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    adapter.updateData(result.data.orEmpty())
                }
                is NetworkResult.Error -> {
                    Toast.makeText(
                        this,
                        result.message ?: "No se pudieron cargar las políticas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }
}