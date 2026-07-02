package com.classdrop.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.classdrop.databinding.ActivityAllSubjectsBinding
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.SubjectsViewModel

class AllSubjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllSubjectsBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: SubjectsViewModel by viewModels()
    private lateinit var adapter: SubjectsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllSubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupHeader()
        setupRecyclerView()
        observeViewModel()

        binding.btnBack.setOnClickListener { finish() }

        viewModel.fetchAllMaterias()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchAllMaterias()
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        binding.tvAvatarInitials.text = initials

        binding.ivNotification.setOnClickListener {
            // Acción para notificaciones
        }

        binding.tvAvatarInitials.setOnClickListener {
            // Podrías navegar al perfil o mostrar un menú
        }
    }

    private fun setupRecyclerView() {
        adapter = SubjectsAdapter { materia ->
            val intent = Intent(this, SubjectDetailActivity::class.java).apply {
                putExtra("SUBJECT_ID", materia.id)
                putExtra("SUBJECT_NAME", materia.nombre)
            }
            startActivity(intent)
        }
        binding.rvSubjects.layoutManager = GridLayoutManager(this, 2)
        binding.rvSubjects.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.materias.observe(this) { materias ->
            adapter.submitList(materias)
        }
        viewModel.error.observe(this) { mensaje ->
            // TODO: mostrar el error (Toast/Snackbar) cuando conectemos el manejo de errores de esta pantalla
        }
    }
}