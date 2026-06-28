package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityAdminHomeBinding
import com.classdrop.ui.auth.LoginActivity
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Punto de entrada del panel de administración. A diferencia de MainActivity
 * (estudiantes), no usa BottomNavBar: cada sección se abre como Activity
 * independiente con navegación de "ir y volver", igual que en el diseño de
 * Figma (header con flecha de regreso en cada pantalla de admin).
 */
class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSubjectCards()
        setupAdminTools()
        
        binding.tvCerrarSesion.setOnClickListener {
            showLogoutConfirmation()
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
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas salir del panel de administración?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                SessionManager(this).clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
