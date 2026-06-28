package com.classdrop.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityReportsBinding
import com.classdrop.model.CommentReport
import com.classdrop.repository.ReportRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var adapter: ReportsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeReports()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = ReportsAdapter(
            onKeep = { report -> showKeepConfirmation(report) },
            onRemove = { report -> showRemoveConfirmation(report) }
        )

        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(this@ReportsActivity)
            adapter = this@ReportsActivity.adapter
        }
    }

    private fun observeReports() {
        ReportRepository.pendingReports.observe(this) { reports ->
            adapter.submitList(reports)
        }
    }

    private fun showKeepConfirmation(report: CommentReport) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Mantener comentario")
            .setMessage("¿Estás seguro de que este comentario es apto para la plataforma?")
            .setPositiveButton("Mantener") { _, _ ->
                ReportRepository.keepComment(report)
                Toast.makeText(this, "Comentario mantenido", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRemoveConfirmation(report: CommentReport) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar comentario")
            .setMessage("¿Deseas eliminar este comentario? Se le notificará al usuario que su contenido no es apto.")
            .setPositiveButton("Eliminar") { _, _ ->
                ReportRepository.removeComment(report)
                Toast.makeText(this, "Comentario eliminado y usuario notificado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
