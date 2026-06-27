package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityAdminHomeBinding
import com.classdrop.ui.auth.LoginActivity
import com.classdrop.utils.SessionManager

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

        binding.cardModeracion.setOnClickListener {
            startActivity(Intent(this, ModerationActivity::class.java))
        }
        binding.cardReportes.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.cardNormas.setOnClickListener {
            startActivity(Intent(this, NormsAdminActivity::class.java))
        }
        binding.tvSeeAllSubjects.setOnClickListener {
            startActivity(Intent(this, SubjectsAdminActivity::class.java))
        }
        binding.tvCerrarSesion.setOnClickListener {
            SessionManager(this).clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}