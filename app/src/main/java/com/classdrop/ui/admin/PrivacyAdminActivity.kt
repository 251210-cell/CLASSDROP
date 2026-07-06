package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityPrivacyAdminBinding
import com.classdrop.model.CommunityRule
import com.classdrop.network.NetworkResult
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.PrivacyViewModel

class PrivacyAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyAdminBinding
    private lateinit var adapter: NormsAdapter
    private lateinit var sessionManager: SessionManager
    private val viewModel: PrivacyViewModel by viewModels()
    private var rulesList = mutableListOf<CommunityRule>()
    private var selectedRule: CommunityRule? = null

    // true mientras el overlay de edición está mostrando el "Mensaje principal"
    // en vez de una política normal de la lista.
    private var isEditingHeader = false

    // El mensaje principal tal como vive en el servidor. Null significa que
    // todavía no se ha creado ninguno (primera vez que se usa la pantalla).
    private var headerRule: CommunityRule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupHeader()
        setupRecyclerView()
        setupListeners()
        setupViewModel()

        viewModel.cargarTodo()
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
        binding.rvPrivacyRules.layoutManager = LinearLayoutManager(this)
        binding.rvPrivacyRules.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnAddPrivacyRule.setOnClickListener {
            showEditOverlay(null)
        }

        binding.btnCancelEdit.setOnClickListener { hideEditOverlay() }

        binding.btnSaveEdit.setOnClickListener {
            saveRule()
        }

        binding.btnEditHeader.setOnClickListener {
            val actual = headerRule ?: CommunityRule(title = "Mensaje principal", description = "")
            showEditOverlay(actual, editingHeader = true)
        }

        binding.btnSuccessDone.setOnClickListener {
            hideEditOverlay()
        }

        binding.btnErrorRetry.setOnClickListener {
            binding.cardError.visibility = View.GONE
            binding.cardEditForm.visibility = View.VISIBLE
        }

        binding.btnCancelDelete.setOnClickListener {
            hideEditOverlay()
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
                        title = "No se pudieron cargar las políticas",
                        message = result.message ?: "Revisa tu conexión e intenta de nuevo.",
                        type = AlertUtils.AlertType.ERROR
                    )
                }
                else -> {}
            }
        }

        viewModel.headerState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    headerRule = result.data
                    binding.tvPrivacyHeaderDesc.text = result.data?.description
                        ?: "Aún no se ha configurado un mensaje principal. Toca el lápiz para crearlo."
                }
                is NetworkResult.Error -> {
                    binding.tvPrivacyHeaderDesc.text = "No se pudo cargar el mensaje principal."
                }
                else -> {}
            }
        }

        viewModel.saveState.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.btnSaveEdit.isEnabled = true
                    binding.cardEditForm.visibility = View.GONE
                    binding.tvSuccessTitle.text = "¡Cambios Guardados!"
                    binding.tvSuccessMessage.text = "La política ha sido actualizada correctamente en el sistema."
                    binding.cardSuccess.visibility = View.VISIBLE

                    val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_in_up)
                    binding.cardSuccess.startAnimation(animation)

                    // Refrescamos desde el servidor para que la lista y el header
                    // queden exactamente como los ve cualquier otro dispositivo.
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
                    binding.tvSuccessTitle.text = "¡Política Eliminada!"
                    binding.tvSuccessMessage.text = "La política ha sido removida permanentemente del sistema."
                    binding.cardSuccess.visibility = View.VISIBLE
                    viewModel.cargarReglas()
                    viewModel.resetDeleteState()
                }
                is NetworkResult.Error -> {
                    binding.cardDeleteConfirm.visibility = View.GONE
                    binding.tvErrorMessage.text = result.message ?: "No se pudo eliminar la política."
                    binding.cardError.visibility = View.VISIBLE
                    viewModel.resetDeleteState()
                }
                else -> {}
            }
        }
    }

    private fun showEditOverlay(rule: CommunityRule?, editingHeader: Boolean = false) {
        selectedRule = rule
        isEditingHeader = editingHeader

        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.GONE

        when {
            editingHeader -> {
                binding.tvOverlayTitle.text = "Editar Mensaje Principal"
                binding.etEditTitle.setText("Mensaje principal")
                binding.etEditDescription.setText(rule?.description)
                binding.etEditTitle.isEnabled = false
            }
            rule != null -> {
                binding.tvOverlayTitle.text = "Editar Política"
                binding.etEditTitle.setText(rule.title)
                binding.etEditDescription.setText(rule.description)
                binding.etEditTitle.isEnabled = true
            }
            else -> {
                binding.tvOverlayTitle.text = "Crear Nueva Política"
                binding.etEditTitle.text = null
                binding.etEditDescription.text = null
                binding.etEditTitle.isEnabled = true
            }
        }

        binding.clEditOverlay.visibility = View.VISIBLE
        binding.cardEditForm.visibility = View.VISIBLE

        val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_in_up)
        binding.cardEditForm.startAnimation(animation)
    }

    private fun hideEditOverlay() {
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_out_down)
        animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                binding.clEditOverlay.visibility = View.GONE
                selectedRule = null
                isEditingHeader = false
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        when {
            binding.cardEditForm.visibility == View.VISIBLE -> binding.cardEditForm.startAnimation(animation)
            binding.cardSuccess.visibility == View.VISIBLE -> binding.cardSuccess.startAnimation(animation)
            binding.cardError.visibility == View.VISIBLE -> binding.cardError.startAnimation(animation)
            binding.cardDeleteConfirm.visibility == View.VISIBLE -> binding.cardDeleteConfirm.startAnimation(animation)
            else -> binding.clEditOverlay.visibility = View.GONE
        }
    }

    private fun saveRule() {
        val title = binding.etEditTitle.text.toString().trim()
        val description = binding.etEditDescription.text.toString().trim()

        val faltaAlgo = description.isBlank() || (!isEditingHeader && title.isBlank())
        if (faltaAlgo) {
            binding.cardEditForm.visibility = View.GONE
            binding.tvErrorMessage.text = "El título y la descripción no pueden estar vacíos."
            binding.cardError.visibility = View.VISIBLE
            return
        }

        // Se deshabilita para evitar doble-tap mientras la petición está en curso;
        // se vuelve a habilitar en los observers de éxito/error.
        binding.btnSaveEdit.isEnabled = false

        if (isEditingHeader) {
            viewModel.guardarMensajePrincipal(headerRule?.id, description)
        } else {
            viewModel.guardarRegla(selectedRule?.id, title, description)
        }
    }

    private fun confirmDeletion(rule: CommunityRule) {
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.VISIBLE
        binding.clEditOverlay.visibility = View.VISIBLE

        val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_in_up)
        binding.cardDeleteConfirm.startAnimation(animation)

        binding.btnConfirmDelete.setOnClickListener {
            viewModel.eliminarRegla(rule.id)
        }
    }
}