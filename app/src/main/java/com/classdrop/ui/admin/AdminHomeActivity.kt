package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityAdminHomeBinding
import com.classdrop.model.MateriaResponse
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.utils.IconMapper
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.SubjectsViewModel

/**
 * Punto de entrada del panel de administración. A diferencia de MainActivity
 * (estudiantes), no usa BottomNavBar: cada sección se abre como Activity
 * independiente con navegación de "ir y volver", igual que en el diseño de
 * Figma (header con flecha de regreso en cada pantalla de admin).
 */
class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: SubjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupSubjectCards()
        setupAdminTools()
        setupHeader()

        viewModel.materias.observe(this) { materias ->
            bindMateriaCards(materias)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresca cada vez que se vuelve a esta pantalla (ej. después de crear/editar/eliminar
        // una materia en otra Activity), para no depender de salir y volver a entrar manualmente.
        viewModel.fetchAllMaterias()
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
            binding.viewNotificationDotAdmin.visibility = View.GONE
            val intent = Intent(this, com.classdrop.ui.notifications.NotificationsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSubjectCards() {
        binding.tvSeeAllSubjects.setOnClickListener {
            startActivity(Intent(this, SubjectsAdminActivity::class.java))
        }
    }

    /** Pinta las primeras 2 materias reales en las tarjetas de bienvenida. */
    private fun bindMateriaCards(materias: List<MateriaResponse>) {
        bindCard(
            materia = materias.getOrNull(0),
            cardView = binding.cardCalculo,
            ivIcon = binding.ivCardMateria1Icon,
            tvTitle = binding.tvCardMateria1Title,
            tvCount = binding.tvCardMateria1Count
        )
        bindCard(
            materia = materias.getOrNull(1),
            cardView = binding.cardProgramacion,
            ivIcon = binding.ivCardMateria2Icon,
            tvTitle = binding.tvCardMateria2Title,
            tvCount = binding.tvCardMateria2Count
        )
    }

    private fun bindCard(
        materia: MateriaResponse?,
        cardView: com.google.android.material.card.MaterialCardView,
        ivIcon: android.widget.ImageView,
        tvTitle: android.widget.TextView,
        tvCount: android.widget.TextView
    ) {
        if (materia == null) {
            cardView.visibility = View.INVISIBLE
            return
        }
        cardView.visibility = View.VISIBLE

        tvTitle.text = materia.nombre
        tvCount.text = "${materia.fileCount ?: 0} archivos"

        val style = IconMapper.fromKey(materia.icono)
        ivIcon.setImageResource(style.drawableRes)
        ivIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(style.bgColor))
        ivIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(style.tintColor))

        cardView.setOnClickListener {
            val intent = Intent(this, SubjectDetailActivity::class.java).apply {
                putExtra("SUBJECT_ID", materia.id)
                putExtra("SUBJECT_NAME", materia.nombre)
            }
            startActivity(intent)
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