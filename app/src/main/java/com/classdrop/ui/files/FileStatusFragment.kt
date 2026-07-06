package com.classdrop.ui.files

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    // El archivo que se está mostrando actualmente (el más reciente que subió el usuario)
    private var archivoActual: FileModel? = null

    // Sondeo automático: mientras el archivo no tenga un resultado final (publicado/rechazado),
    // se le vuelve a preguntar al servidor cada 8s para que el estudiante vea el avance real
    // sin tener que salir y volver a entrar a la pantalla.
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

        filesViewModel.cargarMisArchivos()
    }

    override fun onResume() {
        super.onResume()
        // Por si el estado cambió (el admin lo aprobó/rechazó) mientras esta pantalla
        // estaba en segundo plano.
        filesViewModel.cargarMisArchivos()
    }

    private fun observeMisArchivos() {
        filesViewModel.misArchivos.observe(viewLifecycleOwner) { archivos ->
            // El backend ya los devuelve del más reciente al más antiguo (creado_en DESC),
            // así que el primero es la última subida del usuario: a ese le hacemos seguimiento.
            val ultimo = archivos.firstOrNull()
            archivoActual = ultimo

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

        when (archivo.estado) {
            "pendiente" -> updateStatusUI(DocumentStatus.RECEIVED)
            "escaneando" -> updateStatusUI(DocumentStatus.SCANNING)
            "revision_calidad" -> updateStatusUI(DocumentStatus.QUALITY_CHECK)
            "publicado" -> updateStatusUI(DocumentStatus.PUBLISHED)
            "rechazado" -> showRejectionUI(archivo.motivoRechazo)
            else -> updateStatusUI(DocumentStatus.RECEIVED)
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
    }

    enum class DocumentStatus { RECEIVED, SCANNING, QUALITY_CHECK, PUBLISHED }

    private fun updateStatusUI(status: DocumentStatus) {
        if (_binding == null) return

        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.primary)
        val colorPlaceholder = ContextCompat.getColor(requireContext(), R.color.placeholder)

        resetAllSteps(colorPlaceholder)

        when (status) {
            DocumentStatus.RECEIVED -> {
                setActiveStep(binding.ivStep1, binding.tvTitleStep1, colorPrimary)
            }
            DocumentStatus.SCANNING -> {
                setCompletedStep(binding.ivStep1, binding.line1, colorPrimary)
                setActiveStep(binding.ivStep2, binding.tvTitleStep2, colorPrimary)
            }
            DocumentStatus.QUALITY_CHECK -> {
                setCompletedStep(binding.ivStep1, binding.line1, colorPrimary)
                setCompletedStep(binding.ivStep2, binding.line2, colorPrimary)
                setActiveStep(binding.ivStep3, binding.tvTitleStep3, colorPrimary)
                binding.tvLabelStep3.text = "En revisión por administrador"
                binding.tvLabelStep3.setTextColor(colorPrimary)
                binding.tvDescriptionStep3.visibility = View.VISIBLE
            }
            DocumentStatus.PUBLISHED -> {
                setCompletedStep(binding.ivStep1, binding.line1, colorPrimary)
                setCompletedStep(binding.ivStep2, binding.line2, colorPrimary)
                setCompletedStep(binding.ivStep3, binding.line3, colorPrimary)
                setActiveStep(binding.ivStep4, binding.tvTitleStep4, colorPrimary)
            }
        }
    }

    private fun setActiveStep(icon: View, title: android.widget.TextView, color: Int) {
        icon.backgroundTintList = ColorStateList.valueOf(color)
        title.setTextColor(color)
    }

    private fun setCompletedStep(icon: View, line: View, color: Int) {
        icon.backgroundTintList = ColorStateList.valueOf(color)
        line.backgroundTintList = ColorStateList.valueOf(color)
        if (icon is android.widget.ImageView) {
            icon.setImageResource(R.drawable.ic_check_circle)
        }
    }

    private fun resetAllSteps(color: Int) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.placeholder)
        binding.ivStep1.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep1.setImageResource(R.drawable.ic_check_circle)
        binding.line1.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep2.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep2.setImageResource(R.drawable.ic_status_shield)
        binding.line2.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep3.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep3.setImageResource(R.drawable.ic_nav_notes)
        binding.line3.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep4.backgroundTintList = ColorStateList.valueOf(color)

        binding.tvTitleStep1.setTextColor(textColor)
        binding.tvTitleStep2.setTextColor(textColor)
        binding.tvTitleStep3.setTextColor(textColor)
        binding.tvTitleStep4.setTextColor(textColor)
        binding.tvLabelStep3.text = "Pendiente de escaneo previo"
        binding.tvLabelStep3.setTextColor(textColor)
        binding.tvDescriptionStep3.visibility = View.GONE
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