package com.classdrop.ui.admin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivityNormsAdminBinding
import com.classdrop.model.CommunityRule
import com.classdrop.network.NetworkResult
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.NotificationBadgeUtil
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.NormsViewModel

class NormsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNormsAdminBinding
    private lateinit var adapter: NormsAdapter
    private lateinit var sessionManager: SessionManager
    private val viewModel: NormsViewModel by viewModels()
    private var rulesList = mutableListOf<CommunityRule>()
    private var selectedRule: CommunityRule? = null

    // true mientras el overlay de edición está mostrando el "Régimen Sancionatorio"
    // en vez de una norma normal de la lista.
    private var isEditingSanctions = false

    // El régimen sancionatorio tal como vive en el servidor. Null significa que
    // todavía no se ha creado ninguno.
    private var sanctionsRule: CommunityRule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNormsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        createNotificationChannel()
        setupHeader()
        setupRecyclerView()
        setupListeners()
        setupViewModel()

        viewModel.cargarTodo()
    }

    override fun onResume() {
        super.onResume()
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

    private fun setupRecyclerView() {
        adapter = NormsAdapter(
            rules = rulesList,
            onEditClick = { rule -> showEditOverlay(rule) },
            onDeleteClick = { rule -> confirmDeletion(rule) }
        )
        binding.rvNorms.layoutManager = LinearLayoutManager(this)
        binding.rvNorms.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnAddRule.setOnClickListener {
            showEditOverlay(null)
        }

        binding.btnCancelEdit.setOnClickListener { hideOverlay() }

        binding.btnSaveEdit.setOnClickListener {
            saveRule()
        }

        binding.btnSupport.setOnClickListener {
            contactSupport()
        }

        binding.btnEditSanctions.setOnClickListener {
            val actual = sanctionsRule ?: CommunityRule(title = "Régimen Sancionatorio", description = "")
            showEditOverlay(actual, editingSanctions = true)
        }

        binding.btnCancelDelete.setOnClickListener {
            hideOverlay()
        }

        binding.btnSuccessDone.setOnClickListener {
            hideOverlay()
        }

        binding.btnErrorRetry.setOnClickListener {
            binding.cardError.visibility = View.GONE
            binding.cardEditForm.visibility = View.VISIBLE
        }

        binding.clEditOverlay.setOnClickListener {
            // Evita que los clics pasen a través del fondo oscuro
        }
    }

    private fun setupViewModel() {
        viewModel.rulesState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    rulesList = result.data.orEmpty().toMutableList()
                    adapter.updateData(rulesList)
                }
                is NetworkResult.Error -> {
                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "No se pudieron cargar las normas",
                        message = result.message ?: "Revisa tu conexión e intenta de nuevo.",
                        type = AlertUtils.AlertType.ERROR
                    )
                }
                else -> {}
            }
        }

        viewModel.sanctionsState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    sanctionsRule = result.data
                    binding.tvSanctionsDescription.text = result.data?.description
                        ?: "Aún no se ha configurado el régimen sancionatorio. Toca el lápiz para crearlo."
                }
                is NetworkResult.Error -> {
                    binding.tvSanctionsDescription.text = "No se pudo cargar el régimen sancionatorio."
                }
                else -> {}
            }
        }

        viewModel.saveState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.btnSaveEdit.isEnabled = true
                    binding.cardEditForm.visibility = View.GONE
                    binding.tvSuccessTitle.text = if (isEditingSanctions) "¡Régimen Actualizado!" else "¡Publicado con Éxito!"
                    binding.tvSuccessMessage.text = "La norma ha sido actualizada y los cambios son visibles para todos."
                    binding.cardSuccess.visibility = View.VISIBLE

                    sendUpdateNotification(
                        if (isEditingSanctions) "El régimen sancionatorio fue actualizado"
                        else "Reglamento de la comunidad actualizado"
                    )

                    // Refrescamos desde el servidor para que la lista y el régimen
                    // sancionatorio queden como los ve cualquier otro dispositivo.
                    viewModel.cargarTodo()
                    viewModel.resetSaveState()
                }
                is NetworkResult.Error -> {
                    binding.btnSaveEdit.isEnabled = true
                    binding.cardEditForm.visibility = View.GONE
                    binding.tvErrorMessage.text = result.message ?: "No se pudieron guardar los cambios. Intenta de nuevo."
                    binding.cardError.visibility = View.VISIBLE
                    viewModel.resetSaveState()
                }
                else -> {}
            }
        }

        viewModel.deleteState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.cardDeleteConfirm.visibility = View.GONE
                    binding.tvSuccessTitle.text = "¡Norma Eliminada!"
                    binding.tvSuccessMessage.text = "La norma ha sido eliminada permanentemente y se ha notificado a los usuarios."
                    binding.cardSuccess.visibility = View.VISIBLE
                    sendUpdateNotification("Una norma ha sido eliminada")
                    viewModel.cargarReglas()
                    viewModel.resetDeleteState()
                }
                is NetworkResult.Error -> {
                    binding.cardDeleteConfirm.visibility = View.GONE
                    binding.tvErrorMessage.text = result.message ?: "No se pudo eliminar la norma."
                    binding.cardError.visibility = View.VISIBLE
                    viewModel.resetDeleteState()
                }
                else -> {}
            }
        }
    }

    private fun contactSupport() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte@classdrop.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Consulta: Normas de la Comunidad - Admin")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            AlertUtils.showCustomAlert(
                context = this,
                title = "Error de Contacto",
                message = "No se encontró una aplicación de correo instalada para realizar esta acción.",
                type = AlertUtils.AlertType.ERROR
            )
        }
    }

    private fun confirmDeletion(rule: CommunityRule) {
        selectedRule = rule
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.VISIBLE
        binding.clEditOverlay.visibility = View.VISIBLE
        binding.tvDeleteMessage.text = "¿Estás seguro de que deseas eliminar permanentemente la norma: ${rule.title}?"

        binding.btnConfirmDelete.setOnClickListener {
            viewModel.eliminarRegla(rule.id)
        }
    }

    private fun showEditOverlay(rule: CommunityRule?, editingSanctions: Boolean = false) {
        selectedRule = rule
        isEditingSanctions = editingSanctions

        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.GONE

        when {
            editingSanctions -> {
                binding.tvOverlayTitle.text = "Editar Régimen Sancionatorio"
                binding.etEditRuleTitle.setText("Régimen Sancionatorio")
                binding.etEditRuleDescription.setText(rule?.description)
                binding.etEditRuleTitle.isEnabled = false
            }
            rule != null -> {
                binding.tvOverlayTitle.text = "Editar Norma"
                binding.etEditRuleTitle.setText(rule.title)
                binding.etEditRuleDescription.setText(rule.description)
                binding.etEditRuleTitle.isEnabled = true
            }
            else -> {
                binding.tvOverlayTitle.text = "Crear Nueva Norma"
                binding.etEditRuleTitle.text = null
                binding.etEditRuleDescription.text = null
                binding.etEditRuleTitle.isEnabled = true
            }
        }

        binding.cardEditForm.visibility = View.VISIBLE
        binding.clEditOverlay.visibility = View.VISIBLE
    }

    private fun hideOverlay() {
        binding.clEditOverlay.visibility = View.GONE
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.GONE
        selectedRule = null
        isEditingSanctions = false
    }

    private fun saveRule() {
        val title = binding.etEditRuleTitle.text.toString().trim()
        val description = binding.etEditRuleDescription.text.toString().trim()

        val faltaAlgo = description.isBlank() || (!isEditingSanctions && title.isBlank())
        if (faltaAlgo) {
            binding.cardEditForm.visibility = View.GONE
            binding.tvErrorMessage.text = "Por favor completa todos los campos de la norma."
            binding.cardError.visibility = View.VISIBLE
            return
        }

        // Se deshabilita para evitar doble-tap mientras la petición está en curso;
        // se vuelve a habilitar en los observers de éxito/error.
        binding.btnSaveEdit.isEnabled = false

        if (isEditingSanctions) {
            viewModel.guardarSanciones(sanctionsRule?.id, description)
        } else {
            viewModel.guardarRegla(selectedRule?.id, title, description)
        }
    }

    private fun sendUpdateNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "norms_updates")
            .setSmallIcon(R.drawable.ic_status_shield)
            .setContentTitle("Reglamento Actualizado")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Actualizaciones de Normas"
            val descriptionText = "Notificaciones sobre cambios en el reglamento de la comunidad"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("norms_updates", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}