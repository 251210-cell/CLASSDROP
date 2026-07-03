package com.classdrop.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        refreshData()
    }

    private fun refreshData() {
        binding.pbAllSubjects.visibility = View.VISIBLE
        binding.rvSubjects.visibility = View.GONE
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
            startActivity(Intent(this, com.classdrop.ui.notifications.NotificationsActivity::class.java))
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
            binding.pbAllSubjects.visibility = View.GONE
            binding.rvSubjects.visibility = View.VISIBLE
            adapter.submitList(materias)
        }
        
        viewModel.error.observe(this) { mensaje ->
            binding.pbAllSubjects.visibility = View.GONE
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = this,
                title = "Error",
                message = mensaje,
                type = com.classdrop.utils.AlertUtils.AlertType.ERROR
            )
        }
    }
}
