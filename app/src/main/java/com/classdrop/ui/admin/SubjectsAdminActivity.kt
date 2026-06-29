package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivitySubjectsAdminBinding
import com.classdrop.model.Subject
import com.classdrop.repository.SubjectRepository
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.viewmodel.SubjectsViewModel

class SubjectsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectsAdminBinding
    private val viewModel: SubjectsViewModel by viewModels()
    private lateinit var adapter: SubjectsAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        val sessionManager = com.classdrop.utils.SessionManager(this)
        val userName = sessionManager.fetchUserName() ?: "Admin"
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        binding.tvAvatarInitials.text = initials
        binding.tvAvatarInitials.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }
        
        binding.ivNotificationAdmin.setOnClickListener {
            startActivity(Intent(this, com.classdrop.ui.notifications.NotificationsActivity::class.java))
        }

        adapter = SubjectsAdminAdapter(
            onEditClick = { subject ->
                val intent = Intent(this, CreateSubjectActivity::class.java).apply {
                    putExtra("SUBJECT_ID", subject.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { subject ->
                showDeleteConfirmation(subject)
            },
            onSubjectClick = { subject ->
                // Al pulsar la tarjeta, el admin ve los archivos de los usuarios
                val intent = Intent(this, SubjectDetailActivity::class.java).apply {
                    putExtra("SUBJECT_NAME", subject.name)
                    putExtra("FILE_COUNT", subject.fileCount)
                }
                startActivity(intent)
            }
        )

        binding.rvSubjectsAdmin.apply {
            layoutManager = LinearLayoutManager(this@SubjectsAdminActivity)
            adapter = this@SubjectsAdminActivity.adapter
        }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                viewModel.subjects.value?.filter { it.name.lowercase().contains(query) }?.let {
                    adapter.submitList(it)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.fabAddSubject.setOnClickListener {
            startActivity(Intent(this, CreateSubjectActivity::class.java))
        }
    }

    private fun showDeleteConfirmation(subject: Subject) {
        com.classdrop.utils.AlertUtils.showCustomAlert(
            context = this,
            title = "¿Eliminar ${subject.name}?",
            message = "Esta acción no se puede deshacer. Todos los archivos asociados podrían verse afectados.",
            type = com.classdrop.utils.AlertUtils.AlertType.ERROR,
            primaryButtonText = "Eliminar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                viewModel.deleteSubject(subject)
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "¡Eliminado!",
                    message = "La materia se ha eliminado exitosamente.",
                    type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS
                )
            }
        )
    }

    private fun hideOverlay() {
        // Ya no es necesario con el nuevo sistema de alertas
    }

    private fun setupViewModel() {
        viewModel.subjects.observe(this) { subjects ->
            adapter.submitList(subjects)
        }
    }
}
