package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityCommunityRulesBinding
import com.classdrop.network.NetworkResult
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.NormsViewModel

class CommunityRulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityRulesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: UserNormsAdapter
    private val viewModel: NormsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupHeader()
        setupRecyclerView()
        setupViewModel()
        setupListeners()

        // Trae siempre la versión más reciente publicada por el admin.
        viewModel.cargarTodo()
    }

    private fun setupRecyclerView() {
        adapter = UserNormsAdapter(emptyList())
        binding.rvUserNorms.layoutManager = LinearLayoutManager(this)
        binding.rvUserNorms.adapter = adapter
    }

    private fun setupViewModel() {
        // Lista de normas, igual para todos los estudiantes
        viewModel.rulesState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    adapter.updateData(result.data.orEmpty())
                }
                is NetworkResult.Error -> {
                    Toast.makeText(
                        this,
                        result.message ?: "No se pudieron cargar las normas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }

        // Régimen sancionatorio, tal como lo dejó el admin en el servidor
        viewModel.sanctionsState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.tvSanctionsDescription.text = result.data?.description
                        ?: "Aún no se ha publicado el régimen sancionatorio."
                }
                is NetworkResult.Error -> {
                    binding.tvSanctionsDescription.text = "No se pudo cargar el régimen sancionatorio."
                }
                else -> {}
            }
        }
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
            AlertUtils.showCustomAlert(
                context = this,
                title = "Error",
                message = "No se encontró una aplicación de correo en este dispositivo",
                type = AlertUtils.AlertType.ERROR
            )
        }
    }
}