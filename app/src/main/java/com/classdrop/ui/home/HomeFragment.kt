package com.classdrop.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.classdrop.databinding.FragmentHomeBinding
import com.classdrop.R
import com.classdrop.model.UserRole
import com.classdrop.model.Subject
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.ui.main.MainActivity
import com.classdrop.ui.notifications.NotificationsActivity
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.SubjectsViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: SubjectsViewModel by viewModels()
    private lateinit var adapter: SubjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val userName = sessionManager.fetchUserName()
        val userRole = sessionManager.fetchUserRole()

        if (userRole == UserRole.ADMIN) {
            // UI para Admin
            binding.adminBannerCard.visibility = View.VISIBLE
            binding.adminToolsLayout.visibility = View.VISIBLE
            // En HomeFragment (Estudiante view), ivNotification está dentro de un FrameLayout ahora.
            // Pero si es Admin, el HomeFragment oculta cosas de Estudiante.
            binding.ivNotification.visibility = View.GONE
            binding.viewNotificationDot.visibility = View.GONE
            
            binding.saludoLayout.visibility = View.GONE
            binding.novedadesHeader.visibility = View.GONE
            binding.postCardElena.visibility = View.GONE
            binding.postCardMarco.visibility = View.GONE
        } else {
            // UI para Estudiante (Default)
            binding.adminBannerCard.visibility = View.GONE
            binding.adminToolsLayout.visibility = View.GONE
            binding.ivNotification.visibility = View.VISIBLE
            
            binding.saludoLayout.visibility = View.VISIBLE
            binding.novedadesHeader.visibility = View.VISIBLE
            binding.postCardElena.visibility = View.VISIBLE
            binding.postCardMarco.visibility = View.VISIBLE
            
            binding.tvSaludo.text = "¡Hola, $userName!"
        }
        
        // Configurar iniciales en el avatar
        val initials = if (userName.length >= 2) {
            userName.split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")
        } else if (userName.isNotEmpty()) {
            userName.take(1).uppercase()
        } else {
            "?"
        }
        binding.tvAvatarInitials.text = initials
    }

    private fun setupListeners() {
        // Al hacer clic en el avatar (MA) de la esquina superior derecha, ir al Perfil
        binding.tvAvatarInitials.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.Tab.PROFILE)
        }

        // Al hacer clic en la campana de notificaciones
        binding.ivNotification.setOnClickListener {
            binding.viewNotificationDot.visibility = View.GONE
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        // Navegar a "Ver todas las materias"
        binding.tvSeeAllSubjects.setOnClickListener {
            val userRole = sessionManager.fetchUserRole()
            val activityClass = if (userRole == com.classdrop.model.UserRole.ADMIN) {
                com.classdrop.ui.admin.SubjectsAdminActivity::class.java
            } else {
                AllSubjectsActivity::class.java
            }
            startActivity(Intent(requireContext(), activityClass))
        }

        adapter = SubjectsAdapter { subject ->
            navigateToSubject(subject)
        }
        binding.rvSubjects.adapter = adapter

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            adapter.submitList(subjects)
        }

        setupInteractions()
        setupReactionListeners()
    }

    private fun setupReactionListeners() {
        // IDs for Elena and Marco posts to persist state
        val elenaPostId = "post_elena_derivadas"
        val marcoPostId = "post_marco_recursividad"

        // Initial states from SessionManager
        var isFavoritedElena = sessionManager.isFavorite(elenaPostId)
        var isFavoritedMarco = sessionManager.isFavorite(marcoPostId)
        
        // Update UI with initial states
        updateElenaReactions(false, false, isFavoritedElena)
        updateMarcoReactions(false, false, isFavoritedMarco)

        var isLikedElena = false
        var isDislikedElena = false

        binding.btnLikeElena.setOnClickListener {
            isLikedElena = !isLikedElena
            if (isLikedElena) isDislikedElena = false
            updateElenaReactions(isLikedElena, isDislikedElena, isFavoritedElena)
        }

        binding.btnDislikeElena.setOnClickListener {
            isDislikedElena = !isDislikedElena
            if (isDislikedElena) isLikedElena = false
            updateElenaReactions(isLikedElena, isDislikedElena, isFavoritedElena)
        }

        binding.btnFavoriteElena.setOnClickListener {
            isFavoritedElena = !isFavoritedElena
            sessionManager.toggleFavorite(elenaPostId)
            updateElenaReactions(isLikedElena, isDislikedElena, isFavoritedElena)
        }

        // Similar for Marco
        var isLikedMarco = false
        var isDislikedMarco = false

        binding.btnLikeMarco.setOnClickListener {
            isLikedMarco = !isLikedMarco
            if (isLikedMarco) isDislikedMarco = false
            updateMarcoReactions(isLikedMarco, isDislikedMarco, isFavoritedMarco)
        }

        binding.btnDislikeMarco.setOnClickListener {
            isDislikedMarco = !isDislikedMarco
            if (isDislikedMarco) isLikedMarco = false
            updateMarcoReactions(isLikedMarco, isDislikedMarco, isFavoritedMarco)
        }

        binding.btnFavoriteMarco.setOnClickListener {
            isFavoritedMarco = !isFavoritedMarco
            sessionManager.toggleFavorite(marcoPostId)
            updateMarcoReactions(isLikedMarco, isDislikedMarco, isFavoritedMarco)
        }
    }

    private fun updateElenaReactions(liked: Boolean, disliked: Boolean, favorited: Boolean) {
        val primaryColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.classdrop.R.color.primary)
        val placeholderColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.classdrop.R.color.placeholder)
        val favoriteColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)

        binding.ivLikeElena.imageTintList = android.content.res.ColorStateList.valueOf(if (liked) primaryColor else placeholderColor)
        binding.tvLikeCountElena.setTextColor(if (liked) primaryColor else placeholderColor)
        
        binding.ivDislikeElena.imageTintList = android.content.res.ColorStateList.valueOf(if (disliked) primaryColor else placeholderColor)
        binding.tvDislikeCountElena.setTextColor(if (disliked) primaryColor else placeholderColor)

        binding.btnFavoriteElena.imageTintList = android.content.res.ColorStateList.valueOf(if (favorited) favoriteColor else placeholderColor)
    }

    private fun updateMarcoReactions(liked: Boolean, disliked: Boolean, favorited: Boolean) {
        val primaryColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.classdrop.R.color.primary)
        val placeholderColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.classdrop.R.color.placeholder)
        val favoriteColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)

        binding.ivLikeMarco.imageTintList = android.content.res.ColorStateList.valueOf(if (liked) primaryColor else placeholderColor)
        binding.tvLikeCountMarco.setTextColor(if (liked) primaryColor else placeholderColor)
        
        binding.ivDislikeMarco.imageTintList = android.content.res.ColorStateList.valueOf(if (disliked) primaryColor else placeholderColor)
        binding.tvDislikeCountMarco.setTextColor(if (disliked) primaryColor else placeholderColor)

        binding.btnFavoriteMarco.imageTintList = android.content.res.ColorStateList.valueOf(if (favorited) favoriteColor else placeholderColor)
    }

    private fun setupInteractions() {
        binding.postCardElena.setOnClickListener {
            val intent = Intent(requireContext(), com.classdrop.ui.files.FileDetailActivity::class.java).apply {
                putExtra("FILE_NAME", "Resumen: Derivadas Parciales v2")
                putExtra("FILE_TYPE", "PDF")
                putExtra("FILE_SIZE", "1.8 MB")
            }
            startActivity(intent)
        }

        binding.postCardMarco.setOnClickListener {
            val intent = Intent(requireContext(), com.classdrop.ui.files.FileDetailActivity::class.java).apply {
                putExtra("FILE_NAME", "Guía: Recursividad en C++")
                putExtra("FILE_TYPE", "PDF")
                putExtra("FILE_SIZE", "2.4 MB")
            }
            startActivity(intent)
        }
    }

    private fun navigateToSubject(subject: Subject) {
        val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
            putExtra("SUBJECT_ID", subject.id)
            putExtra("SUBJECT_NAME", subject.name)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}