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
        binding.clOverlay.visibility = android.view.View.VISIBLE
        binding.cardDeleteConfirm.visibility = android.view.View.VISIBLE
        binding.cardSuccess.visibility = android.view.View.GONE
        
        binding.tvDeleteTitle.text = "¿Eliminar ${subject.name}?"
        
        // Animation
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_in_up)
        binding.cardDeleteConfirm.startAnimation(animation)

        binding.btnConfirmDelete.setOnClickListener {
            viewModel.deleteSubject(subject)
            showDeleteSuccess()
        }
        
        binding.btnCancelDelete.setOnClickListener {
            hideOverlay()
        }
    }

    private fun showDeleteSuccess() {
        binding.cardDeleteConfirm.visibility = android.view.View.GONE
        binding.cardSuccess.visibility = android.view.View.VISIBLE
        
        // Animation
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_in_up)
        binding.cardSuccess.startAnimation(animation)

        binding.btnSuccessDone.setOnClickListener {
            hideOverlay()
        }
    }

    private fun hideOverlay() {
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_out_down)
        animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                binding.clOverlay.visibility = android.view.View.GONE
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        if (binding.cardDeleteConfirm.visibility == android.view.View.VISIBLE) {
            binding.cardDeleteConfirm.startAnimation(animation)
        } else if (binding.cardSuccess.visibility == android.view.View.VISIBLE) {
            binding.cardSuccess.startAnimation(animation)
        } else {
            binding.clOverlay.visibility = android.view.View.GONE
        }
    }

    private fun setupViewModel() {
        viewModel.subjects.observe(this) { subjects ->
            adapter.submitList(subjects)
        }
    }
}
