package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityModerationBinding
import com.classdrop.model.FileModel
import com.classdrop.model.ModerationTask
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager
import com.classdrop.utils.TimeUtils
import com.classdrop.viewmodel.FilesViewModel

class ModerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModerationBinding
    private lateinit var adapter: ModerationAdapter
    private lateinit var sessionManager: SessionManager
    private val viewModel: FilesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupHeader()
        observeViewModel()

        viewModel.cargarPendientes()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarPendientes()
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

        adapter = ModerationAdapter(
            onApprove = { task -> showApprovalDialog(task) },
            onReject = { task -> showRejectionDialog(task) }
        )

        binding.rvModeration.apply {
            layoutManager = LinearLayoutManager(this@ModerationActivity)
            adapter = this@ModerationActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.pendientes.observe(this) { archivos ->
            adapter.submitList(archivos.map { it.toModerationTask() })
        }

        viewModel.listError.observe(this) { mensaje ->
            mensaje?.let {
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "No se pudo cargar",
                    message = it,
                    type = AlertUtils.AlertType.ERROR
                )
            }
        }
    }

    private fun FileModel.toModerationTask(): ModerationTask = ModerationTask(
        id = id,
        fileName = titulo,
        userName = autor?.nombreCompleto ?: "Usuario",
        time = TimeUtils.tiempoRelativo(creadoEn),
        // Tu API todavía no genera un motivo de IA legible para mostrar aquí directamente;
        // el riesgo real ya lo calcula el microservicio (riesgoIa) y las funciones de BD.
        flagReason = "Pendiente de revisión manual del administrador.",
        fileUrl = adjuntos?.firstOrNull()?.urlStorage,
        fileType = tipo.uppercase()
    )

    private fun showApprovalDialog(task: ModerationTask) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Aprobar Archivo?",
            message = "¿Deseas validar '${task.fileName}'? El archivo será visible para todos los usuarios.",
            type = AlertUtils.AlertType.CONFIRMATION,
            primaryButtonText = "Aprobar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                viewModel.aprobarArchivo(task.id) { exito, error ->
                    if (exito) {
                        showActionSuccess("Archivo aprobado")
                    } else {
                        showActionError(error ?: "No se pudo aprobar el archivo.")
                    }
                }
            }
        )
    }

    private fun showRejectionDialog(task: ModerationTask) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "¿Rechazar Archivo?",
            message = "¿Deseas rechazar '${task.fileName}'? Se le notificará al usuario.",
            type = AlertUtils.AlertType.ERROR,
            primaryButtonText = "Rechazar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                val motivo = "No cumple con las normas académicas de la plataforma."
                viewModel.rechazarArchivo(task.id, motivo) { exito, error ->
                    if (exito) {
                        showActionSuccess("Archivo rechazado")
                    } else {
                        showActionError(error ?: "No se pudo rechazar el archivo.")
                    }
                }
            }
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

    private fun showActionError(message: String) {
        AlertUtils.showCustomAlert(
            context = this,
            title = "Error",
            message = message,
            type = AlertUtils.AlertType.ERROR,
            primaryButtonText = "Entendido"
        )
    }
}