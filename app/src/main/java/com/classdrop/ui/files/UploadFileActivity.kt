package com.classdrop.ui.files

import androidx.activity.viewModels
import android.net.Uri
import com.classdrop.viewmodel.FilesViewModel
import com.classdrop.viewmodel.UploadState
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.classdrop.R
import com.classdrop.databinding.ActivityUploadBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.SubjectsViewModel


class UploadFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var pagerAdapter: UploadPagerAdapter
    private lateinit var sessionManager: SessionManager
    private val subjectsViewModel: SubjectsViewModel by viewModels()

    private var selectedFileName: String? = null
    private var selectedFileSize: String? = null
    private var enteredUrl: String? = null

    // Selección real (no solo el texto mostrado): esto es lo que se manda a la API
    private var selectedCuatrimestreId: Int? = null
    private var selectedMateriaId: String? = null

    private val viewModel: FilesViewModel by viewModels()
    private var selectedFileUri: Uri? = null
    private var selectedFileMime: String? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            openFilePicker()
        } else {
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = this,
                title = "Permisos Requeridos",
                message = "Se requieren permisos para acceder a tus archivos y poder subirlos.",
                type = com.classdrop.utils.AlertUtils.AlertType.WARNING
            )
        }
    }

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            uri?.let {
                selectedFileUri = it
                selectedFileMime = contentResolver.getType(it) ?: "application/octet-stream"
                val name = it.path?.substringAfterLast('/') ?: "archivo_seleccionado.pdf"
                selectedFileName = name
                pagerAdapter.setSelectedFileName(name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupViewPager()
        setupTabs()
        setupDropdowns()
        setupBottomNav()
        setupHeader()
        handlePreselectedSubject()

        binding.btnPublish.setOnClickListener {
            validateAndPublish()
        }
        viewModel.uploadState.observe(this) { state ->
            when (state) {
                is UploadState.Loading -> binding.btnPublish.isEnabled = false
                is UploadState.Success -> {
                    binding.btnPublish.isEnabled = true
                    startActivity(Intent(this, FileSuccessActivity::class.java).apply {
                        putExtra("FILE_NAME", state.file.titulo)
                    })
                    finish()
                }
                is UploadState.Error -> {
                    binding.btnPublish.isEnabled = true
                    showAlert("Error al subir", state.message)
                }
                UploadState.Idle -> Unit
            }
        }

        subjectsViewModel.fetchCuatrimestres()
        subjectsViewModel.fetchAllMaterias()
    }

    /**
     * La columna `tipo` en la base de datos es un ENUM con solo 4 valores posibles:
     * 'pdf', 'docx', 'url', 'otro'. NO acepta extensiones sueltas como "jpeg" o "png"
     * (esas se validan aparte, en el backend, contra la lista de extensiones permitidas
     * para subir). Aquí solo mapeamos a la categoría correcta para que el INSERT no falle.
     */
    private fun mapMimeATipoArchivo(mime: String?): String {
        return when (mime) {
            "application/pdf" -> "pdf"
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            else -> "otro"
        }
    }

    private fun validateAndPublish() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val quarter = binding.tvSelectedQuarter.text.toString()
        val subject = binding.tvSelectedSubject.text.toString()
        val isFileTab = binding.viewPager.currentItem == 0

        val nameRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$".toRegex()

        if (title.isEmpty()) {
            showAlert("Título requerido", "Por favor ingresa un título para tu apunte.")
            return
        }

        if (!title.matches(nameRegex)) {
            showAlert("Título inválido", "El título no debe contener números ni símbolos especiales.")
            return
        }

        if (description.isEmpty()) {
            showAlert("Descripción requerida", "Cuéntanos un poco de qué trata tu apunte.")
            return
        }

        if (quarter == "Selecciona tu cuatrimestre" || selectedCuatrimestreId == null) {
            showAlert("Selección pendiente", "Por favor selecciona un cuatrimestre.")
            return
        }

        if (subject == "Selecciona una materia" || selectedMateriaId == null) {
            showAlert("Selección pendiente", "Por favor selecciona una materia.")
            return
        }

        if (isFileTab) {
            if (selectedFileName == null) {
                showAlert("Archivo no seleccionado", "Debes seleccionar un archivo para publicar.")
                return
            }
        } else {
            if (enteredUrl.isNullOrBlank()) {
                showAlert("URL requerida", "Por favor ingresa el enlace de tu apunte.")
                return
            }
            if (!android.util.Patterns.WEB_URL.matcher(enteredUrl!!).matches()) {
                showAlert("URL inválida", "Por favor ingresa un enlace válido (ej. https://...)")
                return
            }
        }

        // --- SIMULACIÓN PARA LA DEMO ---
        // Si el título es exactamente "RECHAZAR", mostramos la pantalla de error.
        // En cualquier otro caso, mostramos la de éxito.

        if (isFileTab) {
            val uri = selectedFileUri ?: return showAlert("Archivo no seleccionado", "Selecciona un archivo.")
            viewModel.publicarArchivo(
                uri = uri,
                nombreOriginal = selectedFileName ?: "archivo",
                tipoMime = selectedFileMime ?: "application/octet-stream",
                titulo = title,
                descripcion = description,
                tipo = mapMimeATipoArchivo(selectedFileMime),
                materiaId = selectedMateriaId!!
            )
        } else {
            // TODO: caso URL — lo definimos cuando lleguemos a esa pantalla
        }
    }

    private fun showAlert(title: String, message: String) {
        com.classdrop.utils.AlertUtils.showCustomAlert(
            context = this,
            title = title,
            message = message,
            type = com.classdrop.utils.AlertUtils.AlertType.WARNING
        )
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        binding.tvAvatarInitials.text = initials
        binding.tvAvatarInitials.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SELECT_TAB", "PROFILE")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = UploadPagerAdapter(
            onFileClick = { checkPermissionsAndOpenFile() },
            onUrlChanged = { enteredUrl = it }
        )
        binding.viewPager.adapter = pagerAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val containerWidth = binding.tabContainer.width
                if (containerWidth > 0) {
                    val padding = (12 * resources.displayMetrics.density).toInt()
                    val totalWidth = containerWidth - padding
                    val halfWidth = totalWidth / 2f
                    if (binding.tabSelector.width != halfWidth.toInt()) {
                        binding.tabSelector.layoutParams.width = halfWidth.toInt()
                        binding.tabSelector.requestLayout()
                    }
                    binding.tabSelector.translationX = (position + positionOffset) * halfWidth
                }
            }

            override fun onPageSelected(position: Int) {
                updateTabUI(position == 0)
                updatePublishButton(position == 0)
            }
        })
    }

    private fun setupTabs() {
        binding.tabFile.setOnClickListener { binding.viewPager.smoothScrollTo(0) }
        binding.tabUrl.setOnClickListener { binding.viewPager.smoothScrollTo(1) }
    }

    private fun ViewPager2.smoothScrollTo(item: Int) {
        val containerWidth = binding.tabContainer.width
        val padding = (12 * resources.displayMetrics.density).toInt()
        val halfWidth = (containerWidth - padding) / 2f
        binding.tabSelector.animate()
            .translationX(item * halfWidth)
            .setDuration(250)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
        this.setCurrentItem(item, true)
    }

    private fun updateTabUI(isFileSelected: Boolean) {
        if (isFileSelected) {
            binding.tabFile.apply {
                setTextColor(ContextCompat.getColor(context, R.color.primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.tabUrl.apply {
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        } else {
            binding.tabUrl.apply {
                setTextColor(ContextCompat.getColor(context, R.color.primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.tabFile.apply {
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun updatePublishButton(isFileSelected: Boolean) {
        binding.btnPublish.text = if (isFileSelected) "Publicar Archivo" else "Publicar Enlace"
    }

    private fun checkPermissionsAndOpenFile() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) openFilePicker()
        else requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickFileLauncher.launch(Intent.createChooser(intent, "Selecciona un archivo"))
    }

    private fun setupDropdowns() {
        binding.btnSelectQuarter.setOnClickListener { view ->
            val cuatrimestres = subjectsViewModel.cuatrimestres.value.orEmpty()
            if (cuatrimestres.isEmpty()) {
                showAlert("Cargando...", "Los cuatrimestres todavía se están cargando, intenta de nuevo en un momento.")
                return@setOnClickListener
            }

            val popup = PopupMenu(this, view)
            cuatrimestres.forEachIndexed { index, c -> popup.menu.add(0, index, index, c.nombre) }
            popup.setOnMenuItemClickListener { item ->
                val seleccionado = cuatrimestres[item.itemId]
                selectedCuatrimestreId = seleccionado.id
                binding.tvSelectedQuarter.text = seleccionado.nombre
                binding.tvSelectedQuarter.setTextColor(ContextCompat.getColor(this, R.color.primary))

                // Cambiar de cuatrimestre invalida la materia elegida previamente
                selectedMateriaId = null
                binding.tvSelectedSubject.text = "Selecciona una materia"
                binding.tvSelectedSubject.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                true
            }
            popup.show()
        }

        binding.btnSelectSubject.setOnClickListener { view ->
            val cuatrimestreId = selectedCuatrimestreId
            if (cuatrimestreId == null) {
                showAlert("Selección pendiente", "Primero selecciona un cuatrimestre.")
                return@setOnClickListener
            }

            val materias = subjectsViewModel.materias.value.orEmpty()
                .filter { it.cuatrimestreId == cuatrimestreId }

            if (materias.isEmpty()) {
                showAlert("Sin materias", "Este cuatrimestre todavía no tiene materias registradas.")
                return@setOnClickListener
            }

            val popup = PopupMenu(this, view)
            materias.forEachIndexed { index, m -> popup.menu.add(0, index, index, m.nombre) }
            popup.setOnMenuItemClickListener { item ->
                val seleccionada = materias[item.itemId]
                selectedMateriaId = seleccionada.id
                binding.tvSelectedSubject.text = seleccionada.nombre
                binding.tvSelectedSubject.setTextColor(ContextCompat.getColor(this, R.color.primary))
                true
            }
            popup.show()
        }
    }

    private fun setupBottomNav() {
        val nav = binding.includeBottomNav
        nav.btnNavUpload.getChildAt(0).apply {
            (this as android.widget.ImageView).setColorFilter(ContextCompat.getColor(context, R.color.primary))
        }
        nav.btnNavHome.setOnClickListener { finish() }
        nav.btnNavSearch.setOnClickListener { finish() }
        nav.btnNavNotes.setOnClickListener { finish() }
    }

    private fun handlePreselectedSubject() {
        val preselectedSubjectName = intent.getStringExtra("SELECTED_SUBJECT")
        val preselectedSubjectId = intent.getStringExtra("SELECTED_SUBJECT_ID")

        if (preselectedSubjectName != null) {
            binding.tvSelectedSubject.text = preselectedSubjectName
            binding.tvSelectedSubject.setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        if (preselectedSubjectId != null) {
            selectedMateriaId = preselectedSubjectId

            // En cuanto lleguen las materias y cuatrimestres de la API (sin importar el orden),
            // autoseleccionamos el cuatrimestre real al que pertenece esta materia.
            val intentarAutoseleccionar = {
                val materia = subjectsViewModel.materias.value?.find { it.id == preselectedSubjectId }
                val cuatrimestre = materia?.let { m ->
                    subjectsViewModel.cuatrimestres.value?.find { it.id == m.cuatrimestreId }
                }
                if (cuatrimestre != null) {
                    selectedCuatrimestreId = cuatrimestre.id
                    binding.tvSelectedQuarter.text = cuatrimestre.nombre
                    binding.tvSelectedQuarter.setTextColor(ContextCompat.getColor(this, R.color.primary))
                }
            }
            subjectsViewModel.materias.observe(this) { intentarAutoseleccionar() }
            subjectsViewModel.cuatrimestres.observe(this) { intentarAutoseleccionar() }
        }
    }
}