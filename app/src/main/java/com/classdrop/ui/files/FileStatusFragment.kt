package com.classdrop.ui.files

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.classdrop.databinding.FragmentFileStatusBinding
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
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        
        binding.tvAvatarInitials.text = initials
        
        // Al hacer clic en el avatar, abrir el Perfil
        binding.tvAvatarInitials.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.Tab.PROFILE)
        }
    }

    private fun setupListeners() {
        binding.tvReadRules.setOnClickListener {
            val intent = Intent(requireContext(), CommunityRulesActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewDetails.setOnClickListener {
            // Lógica para ver detalles del archivo
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}