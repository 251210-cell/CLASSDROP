package com.classdrop.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivitySubjectsAdminBinding
import com.classdrop.model.MateriaResponse
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.viewmodel.SubjectsViewModel

class SubjectsAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectsAdminBinding
    private val viewModel: SubjectsViewModel by viewModels()
    private lateinit var adapter: SubjectsAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectsAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupViewModel()

        // Llamamos al método del ViewModel para cargar las materias desde la API al iniciar
        viewModel.fetchAllMaterias()
    }

    override fun onResume() {
        super.onResume()
        // Refresca cada vez que se vuelve a esta pantalla (ej. después de crear/editar una
        // materia en CreateSubjectActivity), para no depender de salir y volver a entrar.
        viewModel.fetchAllMaterias()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        val sessionManager = com.classdrop.utils.SessionManager(this)
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

        // Configuración del adaptador usando el modelo de la API (MateriaResponse)
        adapter = SubjectsAdminAdapter(
            onEditClick = { subject ->
                val intent = Intent(this, CreateSubjectActivity::class.java).apply {
                    // El id de materia es un String (UUID), tal como lo devuelve la API
                    putExtra("SUBJECT_ID", subject.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { subject ->
                showDeleteConfirmation(subject)
            },
            onSubjectClick = { subject ->
                // Al pulsar la tarjeta, el admin ve los archivos de los usuarios
                val intent = Intent(this, SubjectDetailActivity::class.java).apply {
                    putExtra("SUBJECT_NAME", subject.nombre)
                    putExtra("FILE_COUNT", subject.fileCount ?: 0)
                }
                startActivity(intent)
            }
        )

        binding.rvSubjectsAdmin.apply {
            layoutManager = LinearLayoutManager(this@SubjectsAdminActivity)
            adapter = this@SubjectsAdminActivity.adapter
        }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                // Buscamos utilizando la propiedad 'nombre' de MateriaResponse
                viewModel.materias.value?.filter { it.nombre.lowercase().contains(query) }?.let {
                    adapter.submitList(it)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.fabAddSubject.setOnClickListener {
            startActivity(Intent(this, CreateSubjectActivity::class.java))
        }
    }

    // Cambiado el parámetro a 'MateriaResponse' para alinearse con los datos de red
    private fun showDeleteConfirmation(subject: MateriaResponse) {
        com.classdrop.utils.AlertUtils.showCustomAlert(
            context = this,
            title = "¿Eliminar ${subject.nombre}?",
            message = "Esta acción no se puede deshacer. Todos los archivos asociados podrían verse afectados.",
            type = com.classdrop.utils.AlertUtils.AlertType.ERROR,
            primaryButtonText = "Eliminar",
            secondaryButtonText = "Cancelar",
            onPrimaryClick = {
                viewModel.deleteSubject(subject) { exito, mensajeError ->
                    if (exito) {
                        com.classdrop.utils.AlertUtils.showCustomAlert(
                            context = this,
                            title = "¡Eliminado!",
                            message = "La materia se ha eliminado exitosamente.",
                            type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS
                        )
                    } else {
                        com.classdrop.utils.AlertUtils.showCustomAlert(
                            context = this,
                            title = "No se pudo eliminar",
                            message = mensajeError ?: "Ocurrió un error inesperado.",
                            type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                        )
                    }
                }
            }
        )
    }

    private fun hideOverlay() {
        // Ya no es necesario con el nuevo sistema de alertas
    }

    private fun setupViewModel() {
        // Observamos 'materias' en lugar de 'subjects' para recibir la lista de la API
        viewModel.materias.observe(this) { listaMaterias ->
            adapter.submitList(listaMaterias)
        }
    }
}