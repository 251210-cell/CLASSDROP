package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityReportsBinding
import com.classdrop.model.Reporte
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.NotificationBadgeUtil
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.ReportsViewModel

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var adapter: ReportsAdapter
    private lateinit var sessionManager: SessionManager
    private val reportsViewModel: ReportsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupHeader()
        setupObservers()
        reportsViewModel.cargarPendientes()
    }

    override fun onResume() {
        super.onResume()
        // Por si el admin resolvió reportes, salió, y volvió: refrescamos para
        // no mostrar una cola desactualizada.
        reportsViewModel.cargarPendientes()
        NotificationBadgeUtil.actualizar(this, lifecycleScope, binding.ivNotificationAdmin)
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
            startActivity(Intent(this, com.classdrop.ui.notifications.NotificationsActivity::class.java))
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = ReportsAdapter(
            onKeep = { reporte -> showKeepConfirmation(reporte) },
            onRemove = { reporte -> showRemoveConfirmation(reporte) }
        )

        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(this@ReportsActivity)
            adapter = this@ReportsActivity.adapter
        }
    }

    private fun setupObservers() {
        reportsViewModel.pendientes.observe(this) { reportes ->
            adapter.submitList(reportes)
            binding.rvReports.visibility = if (reportes.isEmpty()) View.GONE else View.VISIBLE
            binding.tvEmptyState.visibility = if (reportes.isEmpty()) View.VISIBLE else View.GONE
        }

        reportsViewModel.isLoading.observe(this) { cargando ->
            binding.pbReports.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        reportsViewModel.errorMensaje.observe(this) { mensaje ->
            if (mensaje != null) {
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "Ocurrió un problema",
                    message = mensaje,
                    type = AlertUtils.AlertType.ERROR,
                    primaryButtonText = "Entendido"
                )
                reportsViewModel.limpiarMensajes()
            }
        }

        reportsViewModel.accionExitosa.observe(this) { mensaje ->
            if (mensaje != null) {
                showActionSuccess(mensaje)
                reportsViewModel.limpiarMensajes()
            }
        }
    }

    private fun showKeepConfirmation(reporte: Reporte) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Mantener contenido?",
            message = "¿Estás seguro de que este contenido es apto para la plataforma? Se restaurará y quedará visible de nuevo.",
            type = AlertUtils.AlertType.CONFIRMATION,
            primaryButtonText = "Mantener",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = { reportsViewModel.mantener(reporte) }
        )
    }

    private fun showRemoveConfirmation(reporte: Reporte) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Eliminar contenido?",
            message = "Esta acción es DEFINITIVA: el contenido se borrará por completo y no se puede deshacer.",
            type = AlertUtils.AlertType.ERROR,
            primaryButtonText = "Eliminar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = { reportsViewModel.eliminar(reporte) }
        )
    }

    private fun showActionSuccess(message: String) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "Éxito",
            message = message,
            type = AlertUtils.AlertType.SUCCESS,
            primaryButtonText = "Entendido"
        )
    }
}