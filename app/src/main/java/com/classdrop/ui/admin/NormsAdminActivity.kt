package com.classdrop.ui.admin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivityNormsAdminBinding
import com.classdrop.model.CommunityRule
import com.classdrop.repository.NormsRepository
import com.classdrop.utils.SessionManager

class NormsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNormsAdminBinding
    private lateinit var adapter: NormsAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var normsRepository: NormsRepository
    private var rulesList = mutableListOf<CommunityRule>()
    private var selectedRule: CommunityRule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNormsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        normsRepository = NormsRepository(this)

        createNotificationChannel()
        setupHeader()
        setupRecyclerView()
        setupListeners()
        loadData()
        displaySanctions()
    }

    private fun displaySanctions() {
        binding.tvSanctionsDescription.text = normsRepository.getSanctions()
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
            val currentSanctions = normsRepository.getSanctions()
            val sanctionsRule = CommunityRule(
                id = "sanctions",
                title = "Régimen Sancionatorio",
                description = currentSanctions
            )
            showEditOverlay(sanctionsRule)
        }

        binding.btnCancelDelete.setOnClickListener {
            hideOverlay()
        }

        binding.btnConfirmDelete.setOnClickListener {
            selectedRule?.let { rule ->
                rulesList.remove(rule)
                normsRepository.saveRules(rulesList)
                adapter.updateData(rulesList.toList())
                sendUpdateNotification("Una norma ha sido eliminada: ${rule.title}")
                
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "¡Norma Eliminada!",
                    message = "La norma ha sido eliminada permanentemente y se ha notificado a los usuarios.",
                    type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS,
                    onPrimaryClick = { hideOverlay() }
                )
            }
        }

        binding.clEditOverlay.setOnClickListener {
            // Prevent clicks from passing through
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
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = this,
                title = "Error de Contacto",
                message = "No se encontró una aplicación de correo instalada para realizar esta acción.",
                type = com.classdrop.utils.AlertUtils.AlertType.ERROR
            )
        }
    }

    private fun confirmDeletion(rule: CommunityRule) {
        selectedRule = rule
        com.classdrop.utils.AlertUtils.showCustomAlert(
            context = this,
            title = "¿Eliminar Norma?",
            message = "¿Estás seguro de que deseas eliminar permanentemente la norma: ${rule.title}?",
            type = com.classdrop.utils.AlertUtils.AlertType.ERROR,
            primaryButtonText = "Eliminar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                rulesList.remove(rule)
                normsRepository.saveRules(rulesList)
                adapter.updateData(rulesList.toList())
                sendUpdateNotification("Una norma ha sido eliminada: ${rule.title}")
                
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "¡Eliminado!",
                    message = "La norma ha sido eliminada exitosamente.",
                    type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS
                )
            }
        )
    }

    private fun showEditOverlay(rule: CommunityRule?) {
        selectedRule = rule
        showEditForm()
        if (rule != null) {
            binding.tvOverlayTitle.text = "Editar Norma"
            binding.etEditRuleTitle.setText(rule.title)
            binding.etEditRuleDescription.setText(rule.description)
        } else {
            binding.tvOverlayTitle.text = "Crear Nueva Norma"
            binding.etEditRuleTitle.text = null
            binding.etEditRuleDescription.text = null
        }
        binding.clEditOverlay.visibility = View.VISIBLE
    }

    private fun showEditForm() {
        binding.cardEditForm.visibility = View.VISIBLE
    }

    private fun hideOverlay() {
        binding.clEditOverlay.visibility = View.GONE
        selectedRule = null
    }

    private fun saveRule() {
        val title = binding.etEditRuleTitle.text.toString()
        val description = binding.etEditRuleDescription.text.toString()

        if (title.isBlank() || description.isBlank()) {
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = this,
                title = "Datos incompletos",
                message = "Por favor completa todos los campos de la norma.",
                type = com.classdrop.utils.AlertUtils.AlertType.WARNING
            )
            return
        }

        val isEditing = selectedRule != null
        if (selectedRule == null) {
            val newRule = CommunityRule(
                id = System.currentTimeMillis().toString(),
                title = title,
                description = description
            )
            rulesList.add(newRule)
            normsRepository.saveRules(rulesList)
            sendUpdateNotification("Nueva norma añadida: $title")
        } else {
            if (selectedRule?.id == "sanctions") {
                normsRepository.saveSanctions(description)
                displaySanctions()
            } else {
                val index = rulesList.indexOfFirst { it.id == selectedRule?.id }
                if (index != -1) {
                    rulesList[index] = selectedRule!!.copy(title = title, description = description)
                }
                normsRepository.saveRules(rulesList)
            }
            sendUpdateNotification("Norma actualizada: $title")
        }

        adapter.updateData(rulesList.toList())
        
        com.classdrop.utils.AlertUtils.showCustomAlert(
            context = this,
            title = if (isEditing) "¡Actualizado!" else "¡Publicado!",
            message = if (isEditing) "La norma ha sido actualizada y los usuarios notificados." else "La nueva norma ha sido creada y los usuarios notificados.",
            type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS,
            onPrimaryClick = { hideOverlay() }
        )
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

    private fun loadData() {
        val savedRules = normsRepository.getRules()
        rulesList = savedRules.toMutableList()
        adapter.updateData(rulesList)
    }
}
