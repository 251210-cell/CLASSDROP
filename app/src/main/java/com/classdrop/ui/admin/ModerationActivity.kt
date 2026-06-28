package com.classdrop.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityModerationBinding
import com.classdrop.model.ModerationStatus
import com.classdrop.model.ModerationTask
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ModerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModerationBinding
    private lateinit var adapter: ModerationAdapter
    private val taskList = mutableListOf<ModerationTask>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadMockTasks()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = ModerationAdapter(
            onApprove = { task -> showApprovalDialog(task) },
            onReject = { task -> showRejectionDialog(task) }
        )

        binding.rvModeration.apply {
            layoutManager = LinearLayoutManager(this@ModerationActivity)
            adapter = this@ModerationActivity.adapter
        }
    }

    private fun loadMockTasks() {
        taskList.addAll(listOf(
            ModerationTask(
                "1", "Examen_Parcial_CII_Final.pdf", "Juan Pérez", "Hace 10 min",
                "Patrón de examen institucional detectado. El contenido coincide estructuralmente con evaluaciones previas del departamento de Cálculo."
            ),
            ModerationTask(
                "2", "Solucionario_Guia.pdf", "Juan Pérez", "Hace 10 min",
                "Patrón de examen institucional detectado. El contenido coincide estructuralmente con evaluaciones previas del departamento de Cálculo."
            ),
            ModerationTask(
                "3", "Apunte c++.pdf", "Juan Pérez", "Hace 10 min",
                "Patrón de examen institucional detectado. El contenido coincide estructuralmente con evaluaciones previas del departamento de Cálculo."
            )
        ))
        adapter.submitList(taskList.toList())
    }

    private fun showApprovalDialog(task: ModerationTask) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Aprobar archivo")
            .setMessage("¿Deseas validar '${task.fileName}'? El archivo será visible para todos los usuarios.")
            .setPositiveButton("Aprobar") { _, _ ->
                processTask(task, ModerationStatus.APPROVED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRejectionDialog(task: ModerationTask) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Rechazar archivo")
            .setMessage("¿Deseas rechazar '${task.fileName}'? Se le notificará al usuario que el contenido no cumple con las normas.")
            .setPositiveButton("Rechazar") { _, _ ->
                processTask(task, ModerationStatus.REJECTED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun processTask(task: ModerationTask, newStatus: ModerationStatus) {
        // En una app real, aquí llamarías a la API
        taskList.remove(task)
        adapter.submitList(taskList.toList())
        
        val message = if (newStatus == ModerationStatus.APPROVED) "Archivo aprobado" else "Archivo rechazado"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
