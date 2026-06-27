package com.classdrop.ui.files

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.classdrop.R
import com.classdrop.databinding.FragmentFileStatusBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.ui.profile.CommunityRulesActivity
import com.classdrop.utils.SessionManager

class FileStatusFragment : Fragment() {

    private var _binding: FragmentFileStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    enum class DocumentStatus { RECEIVED, SCANNING, QUALITY_CHECK, PUBLISHED }

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
        
        // Simulación: Iniciar en RECEIVED y avanzar después de unos segundos
        simulateApiProgress()
    }

    private fun simulateApiProgress() {
        updateStatusUI(DocumentStatus.RECEIVED)
        
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatusUI(DocumentStatus.SCANNING, progress = 40)
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            updateStatusUI(DocumentStatus.SCANNING, progress = 100)
        }, 4000)

        Handler(Looper.getMainLooper()).postDelayed({
            updateStatusUI(DocumentStatus.QUALITY_CHECK)
        }, 6000)
    }

    private fun updateStatusUI(status: DocumentStatus, progress: Int = 0) {
        if (_binding == null) return
        
        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.primary)
        val colorPlaceholder = ContextCompat.getColor(requireContext(), R.color.placeholder)
        val colorOnSurface = ContextCompat.getColor(requireContext(), R.color.on_background)

        resetAllSteps(colorPlaceholder)

        when (status) {
            DocumentStatus.RECEIVED -> {
                setActiveStep(binding.ivStep1, binding.tvTitleStep1, colorPrimary)
            }
            DocumentStatus.SCANNING -> {
                setCompletedStep(binding.ivStep1, binding.line1, colorPrimary)
                setActiveStep(binding.ivStep2, binding.tvTitleStep2, colorPrimary)
                binding.statusProgressBar.visibility = View.VISIBLE
                binding.statusProgressBar.progress = progress
            }
            DocumentStatus.QUALITY_CHECK -> {
                setCompletedStep(binding.ivStep1, binding.line1, colorPrimary)
                setCompletedStep(binding.ivStep2, binding.line2, colorPrimary)
                setActiveStep(binding.ivStep3, binding.tvTitleStep3, colorPrimary)
                binding.tvLabelStep3.setTextColor(colorOnSurface)
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
        binding.line1.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep2.backgroundTintList = ColorStateList.valueOf(color)
        binding.line2.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep3.backgroundTintList = ColorStateList.valueOf(color)
        binding.line3.backgroundTintList = ColorStateList.valueOf(color)
        binding.ivStep4.backgroundTintList = ColorStateList.valueOf(color)
        
        binding.tvTitleStep1.setTextColor(textColor)
        binding.tvTitleStep2.setTextColor(textColor)
        binding.tvTitleStep3.setTextColor(textColor)
        binding.tvTitleStep4.setTextColor(textColor)
        
        binding.statusProgressBar.visibility = View.GONE
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        binding.tvAvatarInitials.text = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        
        binding.tvAvatarInitials.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.Tab.PROFILE)
        }
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