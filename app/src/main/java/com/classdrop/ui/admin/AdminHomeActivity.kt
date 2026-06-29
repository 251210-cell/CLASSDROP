package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityAdminHomeBinding
import com.classdrop.ui.auth.LoginActivity
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.utils.SessionManager

/**
 * Punto de entrada del panel de administración. A diferencia de MainActivity
 * (estudiantes), no usa BottomNavBar: cada sección se abre como Activity
 * independiente con navegación de "ir y volver", igual que en el diseño de
 * Figma (header con flecha de regreso en cada pantalla de admin).
 */
class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupSubjectCards()
        setupAdminTools()
        setupHeader()
    }

    private fun setupHeader() {
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
            val intent = Intent(this, com.classdrop.ui.notifications.NotificationsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSubjectCards() {
        // Al pulsar una materia, el admin puede ver los archivos subidos por los usuarios
        binding.cardCalculo.setOnClickListener {
            val intent = Intent(this, SubjectDetailActivity::class.java).apply {
                putExtra("SUBJECT_NAME", "Cálculo II")
                putExtra("FILE_COUNT", 12)
            }
            startActivity(intent)
        }

        binding.cardProgramacion.setOnClickListener {
            val intent = Intent(this, SubjectDetailActivity::class.java).apply {
                putExtra("SUBJECT_NAME", "Programación")
                putExtra("FILE_COUNT", 8)
            }
            startActivity(intent)
        }

        binding.tvSeeAllSubjects.setOnClickListener {
            startActivity(Intent(this, SubjectsAdminActivity::class.java))
        }
    }

    private fun setupAdminTools() {
        binding.cardModeracion.setOnClickListener {
            startActivity(Intent(this, ModerationActivity::class.java))
        }
        binding.cardReportes.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.cardNormas.setOnClickListener {
            startActivity(Intent(this, NormsAdminActivity::class.java))
        }
        binding.cardPrivacidad.setOnClickListener {
            startActivity(Intent(this, PrivacyAdminActivity::class.java))
        }
    }
}
