package com.classdrop.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.classdrop.databinding.FragmentHomeBinding
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.utils.SessionManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

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
            userName.substring(0, 2).uppercase()
        } else {
            userName.take(1).uppercase()
        }
        binding.tvAvatarInitials.text = initials
    }

    private fun setupListeners() {
        binding.cardCalculo.setOnClickListener { navigateToSubject("Cálculo II") }
        binding.cardProgramacion.setOnClickListener { navigateToSubject("Programación") }
        binding.cardBaseDatos.setOnClickListener { navigateToSubject("Base de Datos") }
        binding.cardAlgebra.setOnClickListener { navigateToSubject("Álgebra") }
    }

    private fun navigateToSubject(subjectName: String) {
        val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
            putExtra("SUBJECT_NAME", subjectName)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
