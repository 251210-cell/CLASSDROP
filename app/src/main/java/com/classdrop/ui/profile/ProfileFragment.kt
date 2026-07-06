package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.classdrop.R
import com.classdrop.databinding.FragmentProfileBinding
import com.classdrop.model.FileModel
import com.classdrop.ui.auth.LoginActivity
import com.classdrop.ui.explore.toPost
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.AuthViewModel
import com.classdrop.viewmodel.FilesViewModel
import com.classdrop.network.NetworkResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val filesViewModel: FilesViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

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
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        filesViewModel.cargarMisArchivos()
        filesViewModel.cargarDescargados()
        filesViewModel.cargarFavoritos()
    }

    private fun setupUserData() {
        val userName = sessionManager.fetchUserName()
        val userEmail = sessionManager.fetchUserEmail()

        binding.tvUserName.text = userName
        binding.tvUserInfoName.text = userName
        binding.tvUserInfoEmail.text = userEmail
        
        // Cargar estado inicial del switch desde la sesión
        binding.switch2FA.isChecked = sessionManager.is2FAEnabled()

        binding.tvAvatarInitials.text = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    private fun setupObservers() {
        // Observador para la generación del código (PASO 1)
        authViewModel.generate2FAState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.switch2FA.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.switch2FA.isEnabled = true
                    showOtpInputDialog() // Pedir el código al usuario
                }
                is NetworkResult.Error -> {
                    binding.switch2FA.isEnabled = true
                    binding.switch2FA.isChecked = false
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observador para la activación final (PASO 2)
        authViewModel.activate2FAState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> { }
                is NetworkResult.Success -> {
                    sessionManager.save2FAEnabled(true)
                    AlertUtils.showCustomAlert(
                        requireContext(),
                        "¡Activado!",
                        "La verificación en dos pasos está lista.",
                        AlertUtils.AlertType.SUCCESS
                    )
                }
                is NetworkResult.Error -> {
                    binding.switch2FA.isChecked = false
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        filesViewModel.misArchivos.observe(viewLifecycleOwner) { archivos ->
            binding.tvUploadsCount.text = archivos.size.toString()
            bindPreview(
                archivo = archivos.firstOrNull(),
                card = binding.cardUploadsPreview,
                empty = binding.tvUploadsEmpty,
                tvTitle = binding.tvUploadsPreviewTitle,
                tvSubtitle = binding.tvUploadsPreviewSubtitle,
                tvType = binding.tvUploadsPreviewType
            )
        }
        
        // ... (resto de observadores de archivos)
    }

    private fun showOtpInputDialog() {
        val input = EditText(requireContext())
        input.hint = "000000"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar Activación")
            .setMessage("Ingresa el código de 6 dígitos que enviamos a tu correo institucional.")
            .setView(input)
            .setPositiveButton("Activar") { _, _ ->
                val code = input.text.toString().trim()
                if (code.length == 6) {
                    authViewModel.activate2FA(code)
                } else {
                    binding.switch2FA.isChecked = false
                    Toast.makeText(context, "El código debe ser de 6 dígitos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { _, _ ->
                binding.switch2FA.isChecked = false
            }
            .setCancelable(false)
            .show()
    }

    private fun bindPreview(
        archivo: FileModel?,
        card: com.google.android.material.card.MaterialCardView,
        empty: android.widget.TextView,
        tvTitle: android.widget.TextView,
        tvSubtitle: android.widget.TextView,
        tvType: android.widget.TextView
    ) {
        if (archivo == null) {
            card.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }
        val post = archivo.toPost()
        card.visibility = View.VISIBLE
        empty.visibility = View.GONE
        tvTitle.text = post.fileName
        tvSubtitle.text = post.time
        tvType.text = post.fileType
    }

    private fun setupListeners() {
        // Switch de 2FA
        binding.switch2FA.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !sessionManager.is2FAEnabled()) {
                // El usuario quiere ACTIVAR el 2FA
                authViewModel.generate2FACode()
            } else if (!isChecked && sessionManager.is2FAEnabled()) {
                // Aquí podrías implementar la lógica para desactivarlo en el backend
                sessionManager.save2FAEnabled(false)
                Toast.makeText(context, "2FA desactivado localmente", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardUploads.setOnClickListener {
            binding.scrollViewProfile.post {
                binding.scrollViewProfile.smoothScrollTo(0, binding.titleUploads.top - 20)
            }
        }

        binding.tvSeeMoreUploads.setOnClickListener {
            val intent = Intent(requireContext(), AllFilesActivity::class.java).apply {
                putExtra("FILE_TYPE", "Mis Archivos")
            }
            startActivity(intent)
        }

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

        binding.ivNotification.setOnClickListener {
            binding.viewNotificationDot.visibility = View.GONE
            startActivity(Intent(requireContext(), com.classdrop.ui.notifications.NotificationsActivity::class.java))
        }
    }

    private fun showLogoutConfirmation() {
        AlertUtils.showCustomAlert(
            context = requireContext(),
            title = "¿Cerrar Sesión?",
            message = "¿Estás seguro de que deseas salir de ClassDrop?",
            type = AlertUtils.AlertType.ERROR,
            primaryButtonText = "Cerrar sesión",
            secondaryButtonText = "Cancelar",
            showIcon = false,
            onPrimaryClick = {
                authViewModel.logout {
                    sessionManager.clearSession()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
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
            AlertUtils.showCustomAlert(
                context = requireContext(),
                title = "Error",
                message = "No se encontró una aplicación de correo en este dispositivo",
                type = AlertUtils.AlertType.ERROR
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
