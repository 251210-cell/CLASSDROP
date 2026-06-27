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

        // Navegar a "Ver todas las materias"
        binding.tvSeeAllSubjects.setOnClickListener {
            val intent = Intent(requireContext(), AllSubjectsActivity::class.java)
            startActivity(intent)
        }

        adapter = SubjectsAdapter { subject ->
            navigateToSubject(subject)
        }
        binding.rvSubjects.adapter = adapter

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            adapter.submitList(subjects)
        }

        setupInteractions()
    }

    private fun setupInteractions() {
        // ... (Se mantiene la lógica de interacciones de Elena y Marco)
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