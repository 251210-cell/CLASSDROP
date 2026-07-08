package com.classdrop.ui.files

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.classdrop.R
import com.classdrop.databinding.FragmentFileStatusBinding
import com.classdrop.model.FileModel
import com.classdrop.ui.main.MainActivity
import com.classdrop.ui.profile.CommunityRulesActivity
import com.classdrop.utils.FileTypeUtils
import com.classdrop.utils.SessionManager
import com.classdrop.utils.TimeUtils
import com.classdrop.viewmodel.FilesViewModel

class FileStatusFragment : Fragment() {

    private var _binding: FragmentFileStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val filesViewModel: FilesViewModel by viewModels()

    private var archivoActual: FileModel? = null

    private val pollingHandler = Handler(Looper.getMainLooper())
    private val pollingRunnable = Runnable { filesViewModel.cargarMisArchivos() }
    private val intervaloSondeoMs = 8000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupHeader()
        setupListeners()
        observeMisArchivos()
        observeLoading()

        filesViewModel.cargarMisArchivos(forceRefresh = true)
    }

    private fun observeLoading() {
        filesViewModel.isLoadingMisArchivos.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && archivoActual == null) {
                binding.loadingStatus.visibility = View.VISIBLE
                binding.contentLayout.alpha = 0.3f
            } else {
                binding.loadingStatus.visibility = View.GONE
                binding.contentLayout.alpha = 1.0f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        filesViewModel.cargarMisArchivos()
    }

    private fun observeMisArchivos() {
        filesViewModel.misArchivos.observe(viewLifecycleOwner) { archivos ->
            val ultimo = archivos.firstOrNull()
            
            if (ultimo?.id != archivoActual?.id) {
                archivoActual = ultimo
            }

            if (ultimo == null) {
                showEmptyState()
                detenerSondeo()
                return@observe
            }

            renderEstado(ultimo)

            val esResultadoFinal = ultimo.estado == "publicado" || ultimo.estado == "rechazado"
            if (esResultadoFinal) detenerSondeo() else programarSondeo()
        }

        filesViewModel.listError.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun programarSondeo() {
        pollingHandler.removeCallbacks(pollingRunnable)
        pollingHandler.postDelayed(pollingRunnable, intervaloSondeoMs)
    }

    private fun detenerSondeo() {
        pollingHandler.removeCallbacks(pollingRunnable)
    }

    private fun renderEstado(archivo: FileModel) {
        if (_binding == null) return
        binding.tvFileNameStatus.text = archivo.titulo
        binding.tvTimeReceived.text = "Recibido: ${TimeUtils.tiempoRelativo(archivo.creadoEn)}"
        binding.tvFilePages.text = resumenAdjunto(archivo)
        binding.btnViewDetails.isEnabled = true

        val type = archivo.tipo.uppercase()
        val context = requireContext()
        when (type) {
            "PDF" -> {
                binding.ivSummaryIcon.setImageResource(R.drawable.ic_file_doc)
                binding.ivSummaryIconContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.file_pdf_bg))
                binding.ivSummaryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.file_pdf_text))
            }
            "URL" -> {
                binding.ivSummaryIcon.setImageResource(R.drawable.ic_link)
                binding.ivSummaryIconContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.file_teal_bg))
                binding.ivSummaryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.file_teal_text))
            }
            "DOCX", "DOC" -> {
                binding.ivSummaryIcon.setImageResource(R.drawable.ic_file_doc)
                binding.ivSummaryIconContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.file_pink_bg))
                binding.ivSummaryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.file_pink_text))
            }
            else -> {
                binding.ivSummaryIcon.setImageResource(R.drawable.ic_file_doc)
                binding.ivSummaryIconContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface_variant))
                binding.ivSummaryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary))
            }
        }

        // MAREO DE ESTADOS: Si el archivo ya existe en la DB, el paso 1 (Recibido) ya es un éxito.
        // Por eso, "pendiente" ya activa el paso 2 (Escaneo).
        when (archivo.estado) {
            "pendiente", "escaneando" -> updateStatusUI(DocumentStatus.SCANNING)
            "revision_calidad" -> updateStatusUI(DocumentStatus.QUALITY_CHECK)
            "publicado" -> updateStatusUI(DocumentStatus.PUBLISHED)
            "rechazado" -> showRejectionUI(archivo.motivoRechazo)
            else -> updateStatusUI(DocumentStatus.SCANNING)
        }
    }

    private fun resumenAdjunto(archivo: FileModel): String {
        val adjunto = archivo.adjuntos?.firstOrNull() ?: return archivo.tipo.uppercase()
        adjunto.numPaginas?.let { if (it > 0) return "$it páginas" }
        val mb = adjunto.tamanoBytes / (1024.0 * 1024.0)
        return if (mb >= 0.1) "%.1f MB".format(mb) else "${adjunto.tamanoBytes / 1024} KB"
    }

    private fun showEmptyState() {
        if (_binding == null) return
        binding.tvFileNameStatus.text = "Aún no has subido ningún archivo"
        binding.tvFilePages.text = "Sube tu primer material para hacerle seguimiento aquí"
        binding.tvTimeReceived.text = ""
        binding.btnViewDetails.isEnabled = false
        resetAllSteps(ContextCompat.getColor(requireContext(), R.color.placeholder))
    }

    private fun showRejectionUI(motivo: String?) {
        if (_binding == null) return
        val colorError = ContextCompat.getColor(requireContext(), R.color.error)
        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.primary)
        val colorPlaceholder = ContextCompat.getColor(requireContext(), R.color.placeholder)

        resetAllSteps(colorPlaceholder)
        setCompletedStep(binding.ivStep1, binding.line1, colorPrimary)
        setCompletedStep(binding.ivStep2, binding.line2, colorPrimary)

        binding.ivStep3.backgroundTintList = ColorStateList.valueOf(colorError)
        binding.ivStep3.setImageResource(R.drawable.ic_warning)
        binding.line3.backgroundTintList = ColorStateList.valueOf(colorError)

        binding.tvTitleStep3.text = "Archivo Rechazado"
        binding.tvTitleStep3.setTextColor(colorError)
        binding.tvLabelStep3.text = "Revisión manual completada"
        binding.tvLabelStep3.setTextColor(colorError)
        binding.tvDescriptionStep3.visibility = View.VISIBLE
        binding.tvDescriptionStep3.text = motivo?.takeIf { it.isNotBlank() }
            ?: "Tu archivo no cumple con las normas académicas tras la revisión manual."
        
        binding.statusProgressBar.visibility = View.GONE
    }

    enum class DocumentStatus { RECEIVED, SCANNING, QUALITY_CHECK, PUBLISHED }

    private fun updateStatusUI(status: DocumentStatus) {
        if (_binding == null) return

        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.primary)
        val colorPlaceholder = ContextCompat.getColor(requireContext(), R.color.placeholder)

        resetAllSteps(colorPlaceholder)
        binding.statusProgressBar.visibility = View.GONE
        
        binding.tvTitleStep4.text = "Publicación"
        binding.tvDescriptionStep4.text = "El documento estará disponible pronto."
        binding.tvDescriptionStep4.setTextColor(colorPlaceholder)

        // IMPORTANTE: El paso 1 (Archivo Recibido) siempre se marca como completado 
        // porque si el usuario está en esta pantalla, el servidor ya aceptó el archivo.
        setCompletedStep(binding.ivStep1, binding.line1, colorPrimary)

        when (status) {
            DocumentStatus.SCANNING -> {
                setActiveStep(binding.ivStep2, binding.tvTitleStep2, colorPrimary)
                binding.statusProgressBar.visibility = View.VISIBLE
                binding.statusProgressBar.isIndeterminate = true
                binding.tvDescriptionStep2.setTextColor(colorPrimary)
            }
            DocumentStatus.QUALITY_CHECK -> {
                setCompletedStep(binding.ivStep2, binding.line2, colorPrimary)
                setActiveStep(binding.ivStep3, binding.tvTitleStep3, colorPrimary)
                binding.tvLabelStep3.text = "En revisión por administrador"
                binding.tvLabelStep3.setTextColor(colorPrimary)
                binding.tvDescriptionStep3.visibility = View.VISIBLE
                binding.tvDescriptionStep3.setTextColor(colorPrimary)
            }
            DocumentStatus.PUBLISHED -> {
                setCompletedStep(binding.ivStep2, binding.line2, colorPrimary)
                setCompletedStep(binding.ivStep3, binding.line3, colorPrimary)
                setCompletedStep(binding.ivStep4, null, colorPrimary)
                binding.tvTitleStep4.text = "¡Publicado!"
                binding.tvTitleStep4.setTextColor(colorPrimary)
                binding.tvDescriptionStep4.text = "Tu documento ya está disponible para toda la comunidad."
                binding.tvDescriptionStep4.setTextColor(colorPrimary)
            }
            else -> { /* El estado RECEIVED ahora se maneja dentro de SCANNING por defecto */ }
        }
    }

    private fun setActiveStep(icon: View, title: TextView, color: Int) {
        icon.backgroundTintList = ColorStateList.valueOf(color)
        title.setTextColor(color)
    }

    private fun setCompletedStep(icon: View, line: View?, color: Int) {
        icon.backgroundTintList = ColorStateList.valueOf(color)
        line?.backgroundTintList = ColorStateList.valueOf(color)
        if (icon is android.widget.ImageView) {
            icon.setImageResource(R.drawable.ic_check_circle)
            icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun resetAllSteps(color: Int) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.placeholder)
        binding.ivStep1.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep1.setImageResource(R.drawable.ic_check_circle)
        binding.ivStep1.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        binding.line1.backgroundTintList = ColorStateList.valueOf(color)
        
        binding.ivStep2.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep2.setImageResource(R.drawable.ic_status_shield)
        binding.ivStep2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        binding.line2.backgroundTintList = ColorStateList.valueOf(color)
        
        binding.ivStep3.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep3.setImageResource(R.drawable.ic_nav_notes)
        binding.ivStep3.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        binding.line3.backgroundTintList = ColorStateList.valueOf(color)
        
        binding.ivStep4.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep4.setImageResource(R.drawable.ic_app_logo)
        binding.ivStep4.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        binding.tvTitleStep1.setTextColor(textColor)
        binding.tvTitleStep2.setTextColor(textColor)
        binding.tvTitleStep3.setTextColor(textColor)
        binding.tvTitleStep4.setTextColor(textColor)
        binding.tvLabelStep3.text = "Pendiente de escaneo previo"
        binding.tvLabelStep3.setTextColor(textColor)
        binding.tvDescriptionStep3.visibility = View.GONE
        binding.statusProgressBar.visibility = View.GONE
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        binding.tvAvatarInitials.text = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    private fun setupListeners() {
        binding.tvAvatarInitials.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.Tab.PROFILE)
        }

        binding.tvReadRules.setOnClickListener {
            startActivity(Intent(requireContext(), CommunityRulesActivity::class.java))
        }

        binding.btnViewDetails.setOnClickListener {
            val archivo = archivoActual ?: return@setOnClickListener
            val intent = Intent(requireContext(), FileDetailActivity::class.java).apply {
                putExtra("ARCHIVO_ID", archivo.id)
                putExtra("FILE_NAME", archivo.titulo)
                putExtra(
                    "FILE_TYPE",
                    FileTypeUtils.resolverTipoReal(archivo.adjuntos?.firstOrNull(), archivo.tipo)
                )
                putExtra("FILE_URL", archivo.adjuntos?.firstOrNull()?.urlStorage)
                putExtra("FILE_LIKES", archivo.totalLikes)
                putExtra("FILE_DISLIKES", archivo.totalDislikes)
                putExtra("FILE_DOWNLOADS", archivo.totalDescargas)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detenerSondeo()
        _binding = null
    }
}
