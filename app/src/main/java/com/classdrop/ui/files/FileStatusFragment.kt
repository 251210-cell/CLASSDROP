package com.classdrop.ui.files

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.classdrop.R
import com.classdrop.databinding.FragmentFileStatusBinding
import com.classdrop.model.ModerationStatus
import com.classdrop.repository.ModerationRepository
import com.classdrop.ui.main.MainActivity
import com.classdrop.ui.profile.CommunityRulesActivity
import com.classdrop.utils.SessionManager

class FileStatusFragment : Fragment() {

    private var _binding: FragmentFileStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

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
        observeModerationStatus()
    }

    private fun observeModerationStatus() {
        ModerationRepository.userFileStatus.observe(viewLifecycleOwner) { task ->
            if (task == null) {
                updateStatusUI(DocumentStatus.RECEIVED)
                binding.tvFileNameStatus.text = "Sin archivos pendientes"
                return@observe
            }

            binding.tvFileNameStatus.text = task.fileName

            when (task.status) {
                ModerationStatus.PENDING -> {
                    // El archivo está siendo revisado por el Admin (después del OCR)
                    updateStatusUI(DocumentStatus.QUALITY_CHECK)
                }
                ModerationStatus.APPROVED -> {
                    updateStatusUI(DocumentStatus.PUBLISHED)
                }
                ModerationStatus.REJECTED -> {
                    showRejectionUI()
                }
            }
        }
    }

    private fun showRejectionUI() {
        val colorError = ContextCompat.getColor(requireContext(), R.color.error)
        binding.tvLabelStep3.text = "Archivo Rechazado"
        binding.tvLabelStep3.setTextColor(colorError)
        binding.ivStep3.setImageResource(R.drawable.ic_warning)
        binding.ivStep3.backgroundTintList = ColorStateList.valueOf(colorError)
        
        // Corrección del ID tvDescriptionStep3
        binding.tvDescriptionStep3.text = "Tu archivo no cumple con las normas académicas tras la revisión manual."
        binding.tvDescriptionStep3.visibility = View.VISIBLE
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
        binding.line2.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep3.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep3.setImageResource(R.drawable.ic_status_shield)
        binding.line3.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep4.backgroundTintList = ColorStateList.valueOf(color)
        
        binding.tvTitleStep1.setTextColor(textColor)
        binding.tvTitleStep2.setTextColor(textColor)
        binding.tvTitleStep3.setTextColor(textColor)
        binding.tvTitleStep4.setTextColor(textColor)
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
        binding.tvReadRules.setOnClickListener {
            startActivity(Intent(requireContext(), CommunityRulesActivity::class.java))
        }

        binding.btnViewDetails.setOnClickListener {
            val intent = Intent(requireContext(), FileDetailActivity::class.java).apply {
                putExtra("FILE_NAME", binding.tvFileNameStatus.text.toString())
                putExtra("FILE_TYPE", "PDF")
                putExtra("FILE_SIZE", "2.4 MB")
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
