package com.classdrop.ui.admin

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.classdrop.R
import com.classdrop.databinding.ActivityCreateSubjectBinding
import com.classdrop.model.Subject
import com.classdrop.repository.SubjectRepository
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.SubjectsViewModel
import kotlinx.coroutines.launch

class CreateSubjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateSubjectBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: SubjectsViewModel by viewModels()
    private lateinit var subjectRepository: SubjectRepository
    private var selectedQuarter: Int? = null
    
    // Valores por defecto
    private var selectedIconRes: Int = R.drawable.ic_mortarboard
    private var selectedBgColor: String = "#EEF2FF"
    private val selectedTintColor: String = "#4F46E5"
    
    private var subjectToEdit: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        subjectRepository = SubjectRepository(this)

        val subjectId = intent.getStringExtra("SUBJECT_ID")
        if (subjectId != null) {
            lifecycleScope.launch {
                subjectToEdit = subjectRepository.getSubjectById(subjectId)
                subjectToEdit?.let { loadSubjectData(it) }
            }
        }

        setupUI()
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

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        // Configuración de Iconos
        val icons = listOf(
            Triple(binding.btnIcon1, R.drawable.ic_subject_code, "#F5F3FF"),
            Triple(binding.btnIcon2, R.drawable.ic_subject_sigma, "#EEF2FF"),
            Triple(binding.btnIcon3, R.drawable.ic_database, "#EFF6FF"),
            Triple(binding.btnIcon4, R.drawable.ic_subject_structure, "#FFF1F2"),
            Triple(binding.btnIcon5, R.drawable.ic_subject_math, "#F0FDFA"),
            Triple(binding.btnIcon6, R.drawable.ic_subject_calc, "#FDF4FF")
        )

        icons.forEach { (card, iconRes, bgColor) ->
            card.setOnClickListener {
                selectIcon(card, iconRes, bgColor, icons.map { it.first })
            }
        }

        // Selección de Cuatrimestre
        val quarterCards = listOf(
            binding.btnQ1, binding.btnQ2, binding.btnQ3, binding.btnQ4, binding.btnQ5,
            binding.btnQ6, binding.btnQ7, binding.btnQ8, binding.btnQ9, binding.btnQ10
        )

        quarterCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectQuarter(index + 1, card, quarterCards)
            }
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etSubjectName.text.toString().trim()
            
            if (name.isEmpty() || selectedQuarter == null) {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Faltan datos",
                    message = "Por favor ingresa el nombre de la materia y selecciona un cuatrimestre.",
                    type = com.classdrop.utils.AlertUtils.AlertType.WARNING
                )
                return@setOnClickListener
            }

            try {
                lifecycleScope.launch {
                    val isEditing = subjectToEdit != null
                    if (!isEditing) {
                        // Crear nueva materia
                        val newSubject = Subject(
                            id = System.currentTimeMillis().toString(),
                            name = name,
                            fileCount = 0,
                            iconRes = selectedIconRes,
                            iconBgColor = selectedBgColor,
                            iconTintColor = "#6366F1", 
                            cuatrimestre = "$selectedQuarter Cuatrimestre"
                        )
                        subjectRepository.addSubject(newSubject)
                    } else {
                        // Actualizar materia existente
                        val updatedSubject = subjectToEdit!!.copy(
                            name = name,
                            iconRes = selectedIconRes,
                            iconBgColor = selectedBgColor,
                            cuatrimestre = "$selectedQuarter Cuatrimestre"
                        )
                        subjectRepository.updateSubject(updatedSubject)
                    }

                    com.classdrop.utils.AlertUtils.showCustomAlert(
                        context = this@CreateSubjectActivity,
                        title = if (isEditing) "¡Actualizado!" else "¡Guardado!",
                        message = if (isEditing) "La materia se ha actualizado exitosamente." else "La nueva materia se ha guardado correctamente.",
                        type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS,
                        onPrimaryClick = { finish() }
                    )
                }
            } catch (e: Exception) {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Error",
                    message = "Hubo un error al procesar la solicitud. Intenta de nuevo.",
                    type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                )
            }
        }
    }

    private fun selectIcon(card: com.google.android.material.card.MaterialCardView, iconRes: Int, bgColor: String, allCards: List<com.google.android.material.card.MaterialCardView>) {
        selectedIconRes = iconRes
        selectedBgColor = bgColor
        allCards.forEach { it.strokeWidth = 0 }
        card.strokeWidth = 4
        card.strokeColor = getColor(R.color.primary)
    }

    private fun selectQuarter(quarter: Int, card: com.google.android.material.card.MaterialCardView, allCards: List<com.google.android.material.card.MaterialCardView>) {
        selectedQuarter = quarter
        
        // Colores reactivos al tema para los botones de cuatrimestre
        val defaultBg = ColorStateList.valueOf(getColor(R.color.surface_variant))
        val defaultText = getColor(R.color.on_surface)
        val selectedBg = ColorStateList.valueOf(getColor(R.color.primary))
        val selectedText = Color.WHITE

        allCards.forEach { 
            it.setCardBackgroundColor(defaultBg) 
            (it.getChildAt(0) as android.widget.TextView).setTextColor(defaultText)
        }
        card.setCardBackgroundColor(selectedBg)
        (card.getChildAt(0) as android.widget.TextView).setTextColor(selectedText)
    }

    private fun loadSubjectData(subject: Subject) {
        binding.tvHeaderTitle.text = "Editar Materia"
        
        binding.etSubjectName.setText(subject.name)
        
        // Cargar cuatrimestre (extrayendo el número del string "X Cuatrimestre")
        val quarterNumber = subject.cuatrimestre.split(" ")[0].toIntOrNull()
        if (quarterNumber != null) {
            val quarterCards = listOf(
                binding.btnQ1, binding.btnQ2, binding.btnQ3, binding.btnQ4, binding.btnQ5,
                binding.btnQ6, binding.btnQ7, binding.btnQ8, binding.btnQ9, binding.btnQ10
            )
            selectQuarter(quarterNumber, quarterCards[quarterNumber - 1], quarterCards)
        }

        // Cargar Icono
        val iconCards = listOf(binding.btnIcon1, binding.btnIcon2, binding.btnIcon3, binding.btnIcon4, binding.btnIcon5, binding.btnIcon6)
        val iconResList = listOf(R.drawable.ic_subject_code, R.drawable.ic_subject_sigma, R.drawable.ic_database, R.drawable.ic_subject_structure, R.drawable.ic_subject_math, R.drawable.ic_subject_calc)
        val bgList = listOf("#F5F3FF", "#EEF2FF", "#EFF6FF", "#FFF1F2", "#F0FDFA", "#FDF4FF")
        
        val iconIndex = iconResList.indexOf(subject.iconRes)
        if (iconIndex != -1) {
            selectIcon(iconCards[iconIndex], iconResList[iconIndex], bgList[iconIndex], iconCards)
        }
    }
}
