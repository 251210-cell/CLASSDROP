package com.classdrop.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.model.Notification
import com.classdrop.model.NotificationType
import com.classdrop.databinding.ActivityNotificationsBinding
import com.classdrop.network.NetworkResult
import com.classdrop.ui.admin.ModerationActivity
import com.classdrop.ui.admin.ReportsActivity
import com.classdrop.ui.files.FileDetailActivity
import com.classdrop.utils.AlertUtils
import com.classdrop.viewmodel.NotificationsViewModel

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationsAdapter
    private val viewModel: NotificationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeNotifications()
        viewModel.fetchNotifications()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.tvMarkAllRead.setOnClickListener {
            viewModel.markAllAsRead()
        }

        adapter = NotificationsAdapter { notification ->
            viewModel.markAsRead(notification.id)
            abrirDestino(notification)
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun abrirDestino(notification: Notification) {
        when {
            // --- Navegación de Administrador ---
            // (Estas siguen basadas en el título porque el backend, por ahora,
            // no manda notificaciones de "archivo pendiente" ni "reporte nuevo";
            // quedan listas para cuando se agreguen esos disparadores.)
            notification.title.contains("archivo pendiente", ignoreCase = true) -> {
                startActivity(Intent(this, ModerationActivity::class.java))
            }
            notification.title.contains("Reporte", ignoreCase = true) -> {
                startActivity(Intent(this, ReportsActivity::class.java))
            }

            // --- Navegación de Estudiante: archivo aprobado o rechazado ---
            // Usa el archivoId real que manda el backend en vez de adivinar
            // el destino por el texto del título.
            (notification.type == NotificationType.SUCCESS || notification.type == NotificationType.ERROR)
                    && notification.archivoId != null -> {
                startActivity(
                    Intent(this, FileDetailActivity::class.java)
                        .putExtra("ARCHIVO_ID", notification.archivoId)
                )
            }
        }
    }

    private fun observeNotifications() {
        viewModel.notificationsState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val notifications = result.data ?: emptyList()
                    if (notifications.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvNotifications.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvNotifications.visibility = View.VISIBLE
                        adapter.submitList(notifications)
                    }
                }
                is NetworkResult.Error -> {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "No se pudieron cargar",
                        message = result.message ?: "Ocurrió un error al cargar tus notificaciones.",
                        type = AlertUtils.AlertType.ERROR
                    )
                }
                else -> {}
            }
        }
    }
}