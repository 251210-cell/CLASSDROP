package com.classdrop.ui.files

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.classdrop.R
import com.classdrop.databinding.ActivityUploadBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager

class UploadFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var pagerAdapter: UploadPagerAdapter
    private lateinit var sessionManager: SessionManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Se requieren permisos para subir archivos", Toast.LENGTH_SHORT).show()
        }
    }

    private var selectedFileName: String? = null
    private var selectedFileSize: String? = null

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            uri?.let {
                // En una app real usaríamos ContentResolver para obtener el nombre real
                val name = it.path?.substringAfterLast('/') ?: "archivo_seleccionado.pdf"
                selectedFileName = name
                selectedFileSize = "1.2 MB" // Mock size
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
            val action = if (binding.viewPager.currentItem == 0) "Archivo" else "Enlace"
            
            // Simular el guardado de datos y navegar al estado
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SELECT_TAB", "STATUS")
                putExtra("FILE_NAME", selectedFileName ?: "Cálculo II - Apuntes") 
                putExtra("FILE_SIZE", if (action == "Archivo") (selectedFileSize ?: "2.4 MB") else "Enlace Externo")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        
        binding.tvAvatarInitials.text = initials
        
        // Al hacer clic en el avatar, ir al perfil (en MainActivity)
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
            onUrlChanged = { /* Handle URL logic */ }
        )
        binding.viewPager.adapter = pagerAdapter
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                
                // Mover el selector físicamente mientras el usuario desliza
                val containerWidth = binding.tabContainer.width
                if (containerWidth > 0) {
                    val padding = (12 * resources.displayMetrics.density).toInt() // 6dp de cada lado
                    val totalWidth = containerWidth - padding
                    val halfWidth = totalWidth / 2f
                    
                    // Ajustar el ancho solo una vez
                    if (binding.tabSelector.width != halfWidth.toInt()) {
                        binding.tabSelector.layoutParams.width = halfWidth.toInt()
                        binding.tabSelector.requestLayout()
                    }
                    
                    // Desplazamiento suave (translationX)
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
        binding.tabFile.setOnClickListener {
            binding.viewPager.smoothScrollTo(0)
        }

        binding.tabUrl.setOnClickListener {
            binding.viewPager.smoothScrollTo(1)
        }
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

        if (allGranted) {
            openFilePicker()
        } else {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
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
            val popup = PopupMenu(this, view)
            val quarters = (1..10).map { "$it° Cuatrimestre" }
            quarters.forEachIndexed { index, s -> popup.menu.add(0, index, index, s) }
            
            popup.setOnMenuItemClickListener { item ->
                binding.tvSelectedQuarter.text = quarters[item.itemId]
                binding.tvSelectedQuarter.setTextColor(ContextCompat.getColor(this, R.color.primary))
                true
            }
            popup.show()
        }

        binding.btnSelectSubject.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            val subjects = listOf("Cálculo II", "Programación", "Base de Datos", "Álgebra", "Física")
            subjects.forEachIndexed { index, s -> popup.menu.add(0, index, index, s) }

            popup.setOnMenuItemClickListener { item ->
                binding.tvSelectedSubject.text = subjects[item.itemId]
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
        val preselectedSubject = intent.getStringExtra("SELECTED_SUBJECT")
        if (preselectedSubject != null) {
            binding.tvSelectedSubject.text = preselectedSubject
            binding.tvSelectedSubject.setTextColor(ContextCompat.getColor(this, R.color.primary))
        }
    }
}