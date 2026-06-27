package com.classdrop.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivitySubjectsAdminBinding
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
                Toast.makeText(this, "Editar: ${subject.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { subject ->
                Toast.makeText(this, "Eliminar: ${subject.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvSubjectsAdmin.apply {
            layoutManager = LinearLayoutManager(this@SubjectsAdminActivity)
            adapter = this@SubjectsAdminActivity.adapter
        }

        // Lógica de búsqueda corregida
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
            Toast.makeText(this, "Agregar nueva materia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        viewModel.subjects.observe(this) { subjects ->
            adapter.submitList(subjects)
        }
    }
}