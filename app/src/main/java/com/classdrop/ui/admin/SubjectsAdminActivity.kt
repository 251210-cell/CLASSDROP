package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivitySubjectsAdminBinding
import com.classdrop.model.Subject
import com.classdrop.repository.SubjectRepository
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.viewmodel.SubjectsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        val userName = sessionManager.fetchUserName()
        val initials = if (userName.length >= 2) {
            userName.split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")
        } else if (userName.isNotEmpty()) {
            userName.take(1).uppercase()
        } else {
            "AD"
        }
        binding.tvAvatarInitials.text = initials

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
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar materia?")
            .setMessage("¿Estás seguro de que deseas eliminar '${subject.name}'? Todos los archivos asociados se perderán.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                SubjectRepository.deleteSubject(subject.id)
                Toast.makeText(this, "Materia eliminada", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupViewModel() {
        viewModel.subjects.observe(this) { subjects ->
            adapter.submitList(subjects)
        }
    }
}
