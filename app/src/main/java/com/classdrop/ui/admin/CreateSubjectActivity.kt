package com.classdrop.ui.admin

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.R
import com.classdrop.databinding.ActivityCreateSubjectBinding
import com.classdrop.model.MateriaResponse
import com.classdrop.utils.IconMapper
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.SubjectsViewModel
import com.google.android.material.card.MaterialCardView

class CreateSubjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateSubjectBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: SubjectsViewModel by viewModels()
    private var selectedQuarter: Int? = null
    private var selectedIconKey: String = IconMapper.opciones().first().key

    private var subjectId: String? = null
    private var subjectToEdit: MateriaResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupHeader()

        subjectId = intent.getStringExtra("SUBJECT_ID")
        subjectId?.let { id ->
            binding.tvHeaderTitle.text = "Editar Materia"
            viewModel.cargarMateriaPorId(id) { materia ->
                if (materia != null) {
                    subjectToEdit = materia
                    loadSubjectData(materia)
                } else {
                    com.classdrop.utils.AlertUtils.showCustomAlert(
                        context = this,
                        title = "No se pudo cargar",
                        message = "No se encontró la materia solicitada.",
                        type = com.classdrop.utils.AlertUtils.AlertType.ERROR,
                        onPrimaryClick = { finish() }
                    )
                }
            }
        }
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

        // El orden de estos botones debe coincidir con IconMapper.opciones()
        // (code, sigma, database, structure, math, calc)
        val iconCards = listOf(
            binding.btnIcon1, binding.btnIcon2, binding.btnIcon3,
            binding.btnIcon4, binding.btnIcon5, binding.btnIcon6
        )
        val opciones = IconMapper.opciones()

        iconCards.forEachIndexed { index, card ->
            val estilo = opciones[index]
            card.setOnClickListener {
                selectIcon(card, estilo.key, iconCards)
            }
        }

        val quarterCards = listOf(
            binding.btnQ1, binding.btnQ2, binding.btnQ3, binding.btnQ4, binding.btnQ5,
            binding.btnQ6, binding.btnQ7, binding.btnQ8, binding.btnQ9, binding.btnQ10
        )

        quarterCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectQuarter(index + 1, card, quarterCards)
            }
        }

        binding.btnSave.setOnClickListener { guardar() }
    }

    private fun guardar() {
        val name = binding.etSubjectName.text.toString().trim()

        if (name.isEmpty() || selectedQuarter == null) {
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = this,
                title = "Faltan datos",
                message = "Por favor ingresa el nombre de la materia y selecciona un cuatrimestre.",
                type = com.classdrop.utils.AlertUtils.AlertType.WARNING
            )
            return
        }

        binding.btnSave.isEnabled = false
        val isEditing = subjectToEdit != null

        val onResult: (Boolean, String?) -> Unit = { exito, mensajeError ->
            binding.btnSave.isEnabled = true
            if (exito) {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = if (isEditing) "¡Actualizado!" else "¡Guardado!",
                    message = if (isEditing) "La materia se ha actualizado exitosamente." else "La nueva materia se ha guardado correctamente.",
                    type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS,
                    onPrimaryClick = { finish() }
                )
            } else {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Error",
                    message = mensajeError ?: "Hubo un error al procesar la solicitud. Intenta de nuevo.",
                    type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                )
            }
        }

        if (!isEditing) {
            viewModel.crearMateria(
                nombre = name,
                cuatrimestreId = selectedQuarter!!,
                icono = selectedIconKey,
                onResult = onResult
            )
        } else {
            viewModel.actualizarMateria(
                id = subjectToEdit!!.id,
                campos = mapOf(
                    "nombre" to name,
                    "cuatrimestreId" to selectedQuarter!!,
                    "icono" to selectedIconKey
                ),
                onResult = onResult
            )
        }
    }

    private fun selectIcon(card: MaterialCardView, key: String, allCards: List<MaterialCardView>) {
        selectedIconKey = key
        allCards.forEach { it.strokeWidth = 0 }
        card.strokeWidth = 4
        card.strokeColor = getColor(R.color.primary)
    }

    private fun selectQuarter(quarter: Int, card: MaterialCardView, allCards: List<MaterialCardView>) {
        selectedQuarter = quarter

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

    private fun loadSubjectData(materia: MateriaResponse) {
        binding.etSubjectName.setText(materia.nombre)

        val quarterCards = listOf(
            binding.btnQ1, binding.btnQ2, binding.btnQ3, binding.btnQ4, binding.btnQ5,
            binding.btnQ6, binding.btnQ7, binding.btnQ8, binding.btnQ9, binding.btnQ10
        )
        if (materia.cuatrimestreId in 1..10) {
            selectQuarter(materia.cuatrimestreId, quarterCards[materia.cuatrimestreId - 1], quarterCards)
        }

        val iconCards = listOf(
            binding.btnIcon1, binding.btnIcon2, binding.btnIcon3,
            binding.btnIcon4, binding.btnIcon5, binding.btnIcon6
        )
        val opciones = IconMapper.opciones()
        val indice = opciones.indexOfFirst { it.key == materia.icono }
        if (indice != -1) {
            selectIcon(iconCards[indice], opciones[indice].key, iconCards)
        }
    }
}