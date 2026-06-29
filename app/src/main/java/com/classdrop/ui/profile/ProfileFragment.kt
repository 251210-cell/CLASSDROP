package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.classdrop.databinding.FragmentProfileBinding
import com.classdrop.ui.auth.LoginActivity
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupUserData()
        setupListeners()
    }

    private fun setupUserData() {
        val userName = sessionManager.fetchUserName()
        val userEmail = sessionManager.fetchUserEmail()
        
        binding.tvUserName.text = userName
        binding.tvUserInfoName.text = userName
        binding.tvUserInfoEmail.text = userEmail

        binding.tvAvatarInitials.text = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    private fun setupListeners() {
        // --- Navegación por Scroll ---
        binding.cardUploads.setOnClickListener {
            binding.scrollViewProfile.post {
                binding.scrollViewProfile.smoothScrollTo(0, binding.titleUploads.top - 20)
            }
        }

        binding.cardDownloads.setOnClickListener {
            binding.scrollViewProfile.post {
                binding.scrollViewProfile.smoothScrollTo(0, binding.titleDownloads.top - 20)
            }
        }

        binding.cardFavorites.setOnClickListener {
            binding.scrollViewProfile.post {
                binding.scrollViewProfile.smoothScrollTo(0, binding.titleFavorites.top - 20)
            }
        }

        // --- Navegación a Vistas Completas (Ver más) ---
        binding.tvSeeMoreUploads.setOnClickListener {
            val intent = Intent(requireContext(), AllFilesActivity::class.java).apply {
                putExtra("FILE_TYPE", "Mis Archivos")
            }
            startActivity(intent)
        }

        binding.tvSeeMoreDownloads.setOnClickListener {
            val intent = Intent(requireContext(), AllFilesActivity::class.java).apply {
                putExtra("FILE_TYPE", "Descargas")
            }
            startActivity(intent)
        }

        binding.tvSeeMoreFavorites.setOnClickListener {
            val intent = Intent(requireContext(), AllFilesActivity::class.java).apply {
                putExtra("FILE_TYPE", "Favoritos")
            }
            startActivity(intent)
        }

        // --- Otros Listeners ---
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.btnPrivacy.setOnClickListener {
            val intent = Intent(requireContext(), PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.btnNorms.setOnClickListener {
            val intent = Intent(requireContext(), CommunityRulesActivity::class.java)
            startActivity(intent)
        }

        binding.tvHelpDescription.setOnClickListener {
            sendEmail()
        }
    }

    private fun showLogoutConfirmation() {
        com.classdrop.utils.AlertUtils.showCustomAlert(
            context = requireContext(),
            title = "¿Cerrar Sesión?",
            message = "¿Estás seguro de que deseas salir de ClassDrop?",
            type = com.classdrop.utils.AlertUtils.AlertType.ERROR, // Ahora con ERROR para que el botón sea rojo
            primaryButtonText = "Cerrar sesión",
            secondaryButtonText = "Cancelar",
            showIcon = false,
            onPrimaryClick = {
                sessionManager.clearSession()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        )
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte.classdrop@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Soporte ClassDrop")
        }
        try {
            startActivity(Intent.createChooser(intent, "Enviar correo con..."))
        } catch (e: Exception) {
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = requireContext(),
                title = "Error",
                message = "No se encontró una aplicación de correo en este dispositivo",
                type = com.classdrop.utils.AlertUtils.AlertType.ERROR
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
