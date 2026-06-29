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
        loadMockData()
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

        binding.btnSuccessDone.setOnClickListener {
            hideOverlay()
        }

        binding.btnErrorRetry.setOnClickListener {
            if (binding.tvErrorTitle.text == "Error de Contacto") {
                hideOverlay()
            } else {
                showEditForm()
            }
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
                
                binding.tvSuccessTitle.text = "¡Norma Eliminada!"
                binding.tvSuccessMessage.text = "La norma ha sido eliminada permanentemente y se ha notificado a los usuarios."
                showSuccess()
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
            // No hay apps de correo, se muestra el overlay de error con mensaje personalizado
            binding.tvErrorTitle.text = "Error de Contacto"
            binding.tvErrorMessage.text = "No se encontró una aplicación de correo instalada para realizar esta acción."
            binding.btnErrorRetry.text = "Entendido"
            binding.btnErrorRetry.setOnClickListener { hideOverlay() }
            showError()
            binding.clEditOverlay.visibility = View.VISIBLE
        }
    }

    private fun confirmDeletion(rule: CommunityRule) {
        selectedRule = rule
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
        binding.cardDeleteConfirm.visibility = View.VISIBLE
        binding.clEditOverlay.visibility = View.VISIBLE
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
        binding.cardDeleteConfirm.visibility = View.GONE
        binding.cardEditForm.visibility = View.VISIBLE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.GONE
    }

    private fun showSuccess() {
        binding.cardDeleteConfirm.visibility = View.GONE
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.VISIBLE
        binding.cardError.visibility = View.GONE
    }

    private fun showError() {
        binding.cardDeleteConfirm.visibility = View.GONE
        binding.cardEditForm.visibility = View.GONE
        binding.cardSuccess.visibility = View.GONE
        binding.cardError.visibility = View.VISIBLE
    }

    private fun hideOverlay() {
        binding.clEditOverlay.visibility = View.GONE
        selectedRule = null
    }

    private fun saveRule() {
        val title = binding.etEditRuleTitle.text.toString()
        val description = binding.etEditRuleDescription.text.toString()

        if (title.isBlank() || description.isBlank()) {
            showError()
            return
        }

        binding.tvSuccessTitle.text = "¡Publicado con Éxito!"

        if (selectedRule == null) {
            val newRule = CommunityRule(
                id = System.currentTimeMillis().toString(),
                title = title,
                description = description
            )
            rulesList.add(newRule)
            normsRepository.saveRules(rulesList)
            binding.tvSuccessMessage.text = "La nueva norma ha sido creada y los usuarios han sido notificados."
            sendUpdateNotification("Nueva norma añadida: $title")
        } else {
            if (selectedRule?.id == "sanctions") {
                normsRepository.saveSanctions(description)
                displaySanctions()
                binding.tvSuccessMessage.text = "El Régimen Sancionatorio ha sido actualizado y los usuarios notificados."
            } else {
                val index = rulesList.indexOfFirst { it.id == selectedRule?.id }
                if (index != -1) {
                    rulesList[index] = selectedRule!!.copy(title = title, description = description)
                }
                normsRepository.saveRules(rulesList)
                binding.tvSuccessMessage.text = "La norma ha sido actualizada y los usuarios han sido notificados."
            }
            sendUpdateNotification("Norma actualizada: $title")
        }

        adapter.updateData(rulesList.toList())
        showSuccess()
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

    private fun loadMockData() {
        val savedRules = normsRepository.getRules()
        if (savedRules.isNotEmpty()) {
            rulesList = savedRules.toMutableList()
        } else {
            rulesList = mutableListOf(
                CommunityRule(
                    id = "1",
                    title = "Propósito y Alcance",
                    description = "ClassDrop es una plataforma diseñada para el intercambio de conocimiento. Este reglamento es vinculante para todo usuario registrado y se aplica a todas las interacciones dentro del ecosistema digital de la plataforma."
                ),
                CommunityRule(
                    id = "2",
                    title = "Uso de la Plataforma",
                    description = "Los usuarios son responsables por el uso y seguridad de sus credenciales de acceso. Se prohíbe la creación de cuentas múltiples para evadir sanciones o manipular sistemas de reputación."
                ),
                CommunityRule(
                    id = "3",
                    title = "Integridad Académica",
                    description = "Queda estrictamente prohibido el intercambio de material que fomente el fraude académico, incluyendo pero no limitado a: exámenes vigentes, respuestas de evaluaciones o cualquier método de suplantación."
                ),
                CommunityRule(
                    id = "4",
                    title = "Propiedad Intelectual",
                    description = "Al subir contenido, el usuario garantiza poseer los derechos necesarios o contar con autorización. ClassDrop respetará las leyes de Copyright, el material que infrinja derechos de autor será removido tras una notificación válida."
                ),
                CommunityRule(
                    id = "5",
                    title = "Comportamiento Social",
                    description = "Se exige un trato respetuoso: no se tolera el acoso, la discriminación por cualquier motivo, ni el lenguaje de odio. Las discusiones deben mantenerse en un marco de crítica constructiva."
                ),
                CommunityRule(
                    id = "6",
                    title = "Moderación y Apelaciones",
                    description = "El equipo de moderación tiene la facultad de retirar contenido y suspender cuentas. Los usuarios afectados tienen derecho a una sola apelación formal a través del Centro de Soporte dentro de las 72hs posteriores a la sanción."
                )
            )
            normsRepository.saveRules(rulesList)
        }
        adapter.updateData(rulesList)
    }
}
