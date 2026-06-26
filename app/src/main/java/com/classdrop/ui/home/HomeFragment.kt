package com.classdrop.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.classdrop.databinding.FragmentHomeBinding
import com.classdrop.model.Subject
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.ui.main.MainActivity
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
        binding.tvSaludo.text = "¡Hola, $userName!"
        
        // Configurar iniciales en el avatar
        val initials = if (userName.length >= 2) {
            userName.split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")
        } else {
            userName.take(1).uppercase()
        }
        binding.tvAvatarInitials.text = initials
    }

    private fun setupListeners() {
        // Al hacer clic en el avatar (MA) de la esquina superior derecha, ir al Perfil
        binding.tvAvatarInitials.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.Tab.PROFILE)
        }

        adapter = SubjectsAdapter { subject ->
            navigateToSubject(subject)
        }
        binding.rvSubjects.adapter = adapter

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            adapter.submitList(subjects)
        }
<<<<<<< Updated upstream

        setupInteractions()
    }

    private fun setupInteractions() {
        // Interacciones para Elena
        var isElenaFavorited = false
        binding.btnFavoriteElena.setOnClickListener {
            isElenaFavorited = !isElenaFavorited
            binding.btnFavoriteElena.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                binding.btnFavoriteElena.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }
            binding.btnFavoriteElena.setColorFilter(
                if (isElenaFavorited) resources.getColor(android.R.color.holo_red_light, null)
                else resources.getColor(com.classdrop.R.color.placeholder, null)
            )
        }

        var elenaLikes = 124
        var isElenaLiked = false
        var elenaDislikes = 45
        var isElenaDisliked = false

        val colorDislike = android.graphics.Color.parseColor("#A855F7")
        val colorPlaceholder = resources.getColor(com.classdrop.R.color.placeholder, null)

        binding.btnLikeElena.setOnClickListener {
            // Si tenía dislike, lo quitamos
            if (isElenaDisliked) {
                isElenaDisliked = false
                elenaDislikes--
                binding.tvDislikeCountElena.text = elenaDislikes.toString()
                binding.ivDislikeElena.setColorFilter(colorPlaceholder)
                binding.tvDislikeCountElena.setTextColor(colorPlaceholder)
            }

            isElenaLiked = !isElenaLiked
            elenaLikes += if (isElenaLiked) 1 else -1
            binding.tvLikeCountElena.text = elenaLikes.toString()
            
            // Usamos el morado del dislike (#A855F7) para el like también
            val color = if (isElenaLiked) colorDislike else colorPlaceholder
            binding.ivLikeElena.setColorFilter(color)
            binding.tvLikeCountElena.setTextColor(color)
            
            binding.btnLikeElena.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                binding.btnLikeElena.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }
        }

        binding.btnDislikeElena.setOnClickListener {
            // Si tenía like, lo quitamos
            if (isElenaLiked) {
                isElenaLiked = false
                elenaLikes--
                binding.tvLikeCountElena.text = elenaLikes.toString()
                binding.ivLikeElena.setColorFilter(colorPlaceholder)
                binding.tvLikeCountElena.setTextColor(colorPlaceholder)
            }

            isElenaDisliked = !isElenaDisliked
            elenaDislikes += if (isElenaDisliked) 1 else -1
            binding.tvDislikeCountElena.text = elenaDislikes.toString()
            
            val color = if (isElenaDisliked) colorDislike else colorPlaceholder
            binding.ivDislikeElena.setColorFilter(color)
            binding.tvDislikeCountElena.setTextColor(color)

            binding.btnDislikeElena.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                binding.btnDislikeElena.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }
        }

        // Interacciones para Marco
        var isMarcoFavorited = false
        binding.btnFavoriteMarco.setOnClickListener {
            isMarcoFavorited = !isMarcoFavorited
            binding.btnFavoriteMarco.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                binding.btnFavoriteMarco.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }
            binding.btnFavoriteMarco.setColorFilter(
                if (isMarcoFavorited) resources.getColor(android.R.color.holo_red_light, null)
                else resources.getColor(com.classdrop.R.color.placeholder, null)
            )
        }

        var marcoLikes = 89
        var isMarcoLiked = false
        var marcoDislikes = 12
        var isMarcoDisliked = false

        binding.btnLikeMarco.setOnClickListener {
            if (isMarcoDisliked) {
                isMarcoDisliked = false
                marcoDislikes--
                binding.tvDislikeCountMarco.text = marcoDislikes.toString()
                binding.ivDislikeMarco.setColorFilter(colorPlaceholder)
                binding.tvDislikeCountMarco.setTextColor(colorPlaceholder)
            }

            isMarcoLiked = !isMarcoLiked
            marcoLikes += if (isMarcoLiked) 1 else -1
            binding.tvLikeCountMarco.text = marcoLikes.toString()
            
            // Usamos el morado del dislike (#A855F7) para el like también
            val color = if (isMarcoLiked) colorDislike else colorPlaceholder
            binding.ivLikeMarco.setColorFilter(color)
            binding.tvLikeCountMarco.setTextColor(color)

            binding.btnLikeMarco.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                binding.btnLikeMarco.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }
        }

        binding.btnDislikeMarco.setOnClickListener {
            if (isMarcoLiked) {
                isMarcoLiked = false
                marcoLikes--
                binding.tvLikeCountMarco.text = marcoLikes.toString()
                binding.ivLikeMarco.setColorFilter(colorPlaceholder)
                binding.tvLikeCountMarco.setTextColor(colorPlaceholder)
            }

            isMarcoDisliked = !isMarcoDisliked
            marcoDislikes += if (isMarcoDisliked) 1 else -1
            binding.tvDislikeCountMarco.text = marcoDislikes.toString()
            
            val color = if (isMarcoDisliked) colorDislike else colorPlaceholder
            binding.ivDislikeMarco.setColorFilter(color)
            binding.tvDislikeCountMarco.setTextColor(color)

            binding.btnDislikeMarco.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                binding.btnDislikeMarco.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }
        }
=======
>>>>>>> Stashed changes
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