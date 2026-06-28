package com.classdrop.ui.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.R
import com.classdrop.databinding.ActivityCreateSubjectBinding
import com.classdrop.model.Subject
import com.classdrop.repository.SubjectRepository

class CreateSubjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateSubjectBinding
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

        val subjectId = intent.getStringExtra("SUBJECT_ID")
        if (subjectId != null) {
            subjectToEdit = SubjectRepository.getSubjectById(subjectId)
        }

        setupUI()
        
        if (subjectToEdit != null) {
            loadSubjectData(subjectToEdit!!)
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
                Toast.makeText(this, "Completa el nombre y cuatrimestre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (subjectToEdit == null) {
                // Crear nueva materia
                val newSubject = Subject(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    fileCount = 0,
                    iconRes = selectedIconRes,
                    iconBgColor = selectedBgColor,
                    iconTintColor = selectedTintColor,
                    cuatrimestre = "$selectedQuarter Cuatrimestre"
                )
                SubjectRepository.addSubject(newSubject)
                Toast.makeText(this, "¡Materia creada!", Toast.LENGTH_SHORT).show()
            } else {
                // Actualizar materia existente
                val updatedSubject = subjectToEdit!!.copy(
                    name = name,
                    iconRes = selectedIconRes,
                    iconBgColor = selectedBgColor,
                    cuatrimestre = "$selectedQuarter Cuatrimestre"
                )
                SubjectRepository.updateSubject(updatedSubject)
                Toast.makeText(this, "¡Materia actualizada!", Toast.LENGTH_SHORT).show()
            }
            
            finish()
        }
    }

    private fun selectIcon(card: com.google.android.material.card.MaterialCardView, iconRes: Int, bgColor: String, allCards: List<com.google.android.material.card.MaterialCardView>) {
        selectedIconRes = iconRes
        selectedBgColor = bgColor
        allCards.forEach { it.strokeWidth = 0 }
        card.strokeWidth = 4
        card.strokeColor = Color.parseColor("#4F46E5")
    }

    private fun selectQuarter(quarter: Int, card: com.google.android.material.card.MaterialCardView, allCards: List<com.google.android.material.card.MaterialCardView>) {
        selectedQuarter = quarter
        allCards.forEach { 
            it.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F1F5F9"))) 
            (it.getChildAt(0) as android.widget.TextView).setTextColor(Color.parseColor("#1E293B"))
        }
        card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#4F46E5")))
        (card.getChildAt(0) as android.widget.TextView).setTextColor(Color.WHITE)
    }

    private fun loadSubjectData(subject: Subject) {
        // Cambiar título de la vista
        // Nota: asumo que el ID del TextView de título en el header es dinámico o puedo encontrarlo. 
        // En el layout es el segundo hijo del headerLayout LinearLayout.
        (binding.headerLayout.getChildAt(1) as? android.widget.TextView)?.text = "Editar Materia"
        
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
