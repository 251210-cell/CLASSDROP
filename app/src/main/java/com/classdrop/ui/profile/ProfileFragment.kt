package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        binding.tvUserName.text = userName
        binding.tvAvatarInitials.text = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    private fun setupListeners() {
        // --- Navegación por Scroll (Desplazamiento suave a la sección) ---
        
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

        // --- Otros Listeners ---
        
        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
        
        // Listeners para "Ver más"
        binding.tvSeeMoreUploads.setOnClickListener {
            Toast.makeText(context, "Abriendo todos tus archivos...", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeMoreDownloads.setOnClickListener {
            Toast.makeText(context, "Abriendo todas tus descargas...", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeMoreFavorites.setOnClickListener {
            Toast.makeText(context, "Abriendo todos tus favoritos...", Toast.LENGTH_SHORT).show()
        }
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
            Toast.makeText(context, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}