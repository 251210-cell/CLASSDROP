package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityPrivacyAdminBinding
import com.classdrop.model.CommunityRule
import com.classdrop.repository.NormsRepository
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.PrivacyViewModel
import kotlinx.coroutines.launch

class PrivacyAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyAdminBinding
    private lateinit var adapter: NormsAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var normsRepository: NormsRepository
    private val viewModel: PrivacyViewModel by viewModels()
    private var rulesList = mutableListOf<CommunityRule>()
    private var selectedRule: CommunityRule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        normsRepository = NormsRepository(this)

        setupHeader()
        setupRecyclerView()
        setupListeners()
        setupViewModel()
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
            val currentHeader = normsRepository.getPrivacyHeader()
            val headerRule = CommunityRule(
                id = "header",
                title = "Mensaje principal",
                description = currentHeader
            )
            showEditOverlay(headerRule)
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
        binding.tvPrivacyHeaderDesc.text = normsRepository.getPrivacyHeader()

        viewModel.privacyRules.observe(this) { rules ->
            if (rules.isEmpty()) {
                loadInitialData()
            } else {
                rulesList = rules.toMutableList()
                adapter.updateData(rules)
            }
        }
    }

    private fun loadInitialData() {
        val initialRules = listOf(
            CommunityRule(
                id = "p1",
                title = "Recopilación de Datos",
                description = "Únicamente recabamos tu nombre completo y tu correo electrónico institucional de la UPChiapas al registrarte. Te garantizamos que ClassDrop no solicita ni trata ningún tipo de datos personales sensibles."
            ),
            CommunityRule(
                id = "p2",
                title = "Uso de la Información",
                description = "Utilizamos los datos exclusivamente para crear tu cuenta, identificarte dentro de la plataforma y hacer visible tu nombre ante los demás miembros al momento de compartir y autorizar tus apuntes."
            ),
            CommunityRule(
                id = "p3",
                title = "Protección de Datos",
                description = "Tus datos están protegidos por el equipo de ClassDrop. Nos comprometemos firmemente a no transferir ni compartir tu información con terceros."
            ),
            CommunityRule(
                id = "p4",
                title = "Tus Derechos",
                description = "Como titular de tus datos, tienes derecho a Acceder, Rectificar, Cancelar u Oponerte (Derechos ARCO) al uso de tu información en cualquier momento. Puedes ejercer estos derechos enviando una solicitud digital con tu credencial de la UPChiapas."
            ),
            CommunityRule(
                id = "p5",
                title = "¿Tienes dudas adicionales?",
                description = "Si tienes alguna duda o quieres ejercer tus derechos ARCO, comunícate con el equipo de The Sync a través de nuestro correo oficial de atención: soporte.classdrop@gmail.com"
            )
        )
        viewModel.saveAllRules(initialRules)
    }

    private fun showEditOverlay(rule: CommunityRule?) {
        selectedRule = rule
        
        // Reset visibility of all cards
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.GONE

        if (rule != null) {
            binding.tvOverlayTitle.text = if (rule.id == "header") "Editar Mensaje Principal" else "Editar Política"
            binding.etEditTitle.setText(rule.title)
            binding.etEditDescription.setText(rule.description)
            binding.etEditTitle.isEnabled = (rule.id != "header")
        } else {
            binding.tvOverlayTitle.text = "Crear Nueva Política"
            binding.etEditTitle.text = null
            binding.etEditDescription.text = null
            binding.etEditTitle.isEnabled = true
        }
        
        binding.clEditOverlay.visibility = View.VISIBLE
        binding.cardEditForm.visibility = View.VISIBLE
        
        // Animation
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
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
        
        // Find which card is currently visible to animate it
        when {
            binding.cardEditForm.visibility == View.VISIBLE -> binding.cardEditForm.startAnimation(animation)
            binding.cardSuccess.visibility == View.VISIBLE -> binding.cardSuccess.startAnimation(animation)
            binding.cardError.visibility == View.VISIBLE -> binding.cardError.startAnimation(animation)
            binding.cardDeleteConfirm.visibility == View.VISIBLE -> binding.cardDeleteConfirm.startAnimation(animation)
            else -> binding.clEditOverlay.visibility = View.GONE
        }
    }

    private fun saveRule() {
        val title = binding.etEditTitle.text.toString()
        val description = binding.etEditDescription.text.toString()

        if (title.isBlank() || description.isBlank()) {
            binding.cardEditForm.visibility = View.GONE
            binding.cardError.visibility = View.VISIBLE
            return
        }

        try {
            if (selectedRule == null) {
                val newRule = CommunityRule(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    description = description
                )
                viewModel.saveRule(newRule)
            } else {
                if (selectedRule?.id == "header") {
                    normsRepository.savePrivacyHeader(description)
                    binding.tvPrivacyHeaderDesc.text = description
                } else {
                    val updatedRule = selectedRule!!.copy(
                        title = title, 
                        description = description
                    )
                    viewModel.saveRule(updatedRule)
                }
            }
            
            // Show Success
            binding.cardEditForm.visibility = View.GONE
            binding.tvSuccessTitle.text = "¡Cambios Guardados!"
            binding.tvSuccessMessage.text = "La política ha sido actualizada correctamente en el sistema."
            binding.cardSuccess.visibility = View.VISIBLE
            
            // Animation for success card
            val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_in_up)
            binding.cardSuccess.startAnimation(animation)
            
        } catch (e: Exception) {
            binding.cardEditForm.visibility = View.GONE
            binding.cardError.visibility = View.VISIBLE
        }
    }

    private fun confirmDeletion(rule: CommunityRule) {
        // Reset visibility of all cards
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.VISIBLE
        binding.clEditOverlay.visibility = View.VISIBLE
        
        // Animation
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, com.classdrop.R.anim.slide_in_up)
        binding.cardDeleteConfirm.startAnimation(animation)

        binding.btnConfirmDelete.setOnClickListener {
            try {
                viewModel.deleteRule(rule)
                
                // Show Success
                binding.cardDeleteConfirm.visibility = View.GONE
                binding.tvSuccessTitle.text = "¡Política Eliminada!"
                binding.tvSuccessMessage.text = "La política ha sido removida permanentemente del sistema."
                binding.cardSuccess.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.cardDeleteConfirm.visibility = View.GONE
                binding.cardError.visibility = View.VISIBLE
            }
        }
    }
}
