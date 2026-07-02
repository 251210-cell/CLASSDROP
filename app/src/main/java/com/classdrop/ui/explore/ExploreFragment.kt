package com.classdrop.ui.explore

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.FragmentExploreBinding
import com.classdrop.model.CuatrimestreResponse
import com.classdrop.model.MateriaResponse
import com.classdrop.ui.home.SubjectsAdapter
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.SubjectsViewModel

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: SubjectsViewModel by viewModels()

    private lateinit var subjectsAdapter: SubjectsAdapter
    private lateinit var suggestionsAdapter: SuggestionsAdapter

    // Cachés locales, alimentadas por los observers del ViewModel
    private var allMaterias: List<MateriaResponse> = emptyList()
    private var allCuatrimestres: List<CuatrimestreResponse> = emptyList()

    // Materias visibles actualmente (filtradas por cuatrimestre), para el popup de selección
    private var materiasDelCuatrimestreSeleccionado: List<MateriaResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupUserData()
        setupSubjectsAdapter()
        setupSuggestions()
        setupSearch()
        setupCuatrimestresRecycler()
        observeViewModel()

        viewModel.fetchAllMaterias()
        viewModel.fetchCuatrimestres()
    }

    private fun observeViewModel() {
        viewModel.materias.observe(viewLifecycleOwner) { materias ->
            allMaterias = materias
            subjectsAdapter.submitList(materias)
        }

        viewModel.cuatrimestres.observe(viewLifecycleOwner) { cuatrimestres ->
            allCuatrimestres = cuatrimestres
            val nombres = cuatrimestres.map { it.nombre }
            binding.rvCuatrimestres.adapter = QuartersAdapter(nombres) { nombreSeleccionado ->
                onCuatrimestreSeleccionado(nombreSeleccionado)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = requireContext(),
                title = "No se pudo cargar",
                message = mensaje,
                type = com.classdrop.utils.AlertUtils.AlertType.ERROR
            )
        }
    }

    private fun setupUserData() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        binding.tvAvatarInitials.text = initials

        binding.tvAvatarInitials.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.Tab.PROFILE)
        }

        binding.ivNotification.setOnClickListener {
            binding.viewNotificationDot.visibility = View.GONE
            startActivity(Intent(requireContext(), com.classdrop.ui.notifications.NotificationsActivity::class.java))
        }
    }

    private fun setupSuggestions() {
        suggestionsAdapter = SuggestionsAdapter { suggestion ->
            binding.etSearch.setText(suggestion.title)
            binding.rvSuggestions.visibility = View.GONE

            val materia = allMaterias.find { it.nombre == suggestion.title }
            abrirDetalleMateria(materia, suggestion.title)
        }
        binding.rvSuggestions.layoutManager = LinearLayoutManager(context)
        binding.rvSuggestions.adapter = suggestionsAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    showSuggestions(query)
                } else {
                    binding.rvSuggestions.visibility = View.GONE
                    subjectsAdapter.submitList(allMaterias)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showSuggestions(query: String) {
        val suggestions = allMaterias
            .filter { it.nombre.contains(query, ignoreCase = true) }
            .map { Suggestion(it.nombre, "Materia") }

        if (suggestions.isNotEmpty()) {
            suggestionsAdapter.submitList(suggestions)
            binding.rvSuggestions.visibility = View.VISIBLE
        } else {
            binding.rvSuggestions.visibility = View.GONE
        }

        filterSubjects(query)
    }

    private fun filterSubjects(query: String) {
        val filteredList = if (query.isEmpty()) {
            allMaterias
        } else {
            allMaterias.filter { it.nombre.contains(query, ignoreCase = true) }
        }
        subjectsAdapter.submitList(filteredList)
    }

    private fun setupCuatrimestresRecycler() {
        binding.rvCuatrimestres.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // El adapter real con datos de la API se asigna en observeViewModel() cuando llegan los cuatrimestres

        binding.btnSelectMateria.setOnClickListener {
            showSubjectsPopupMenu(it)
        }
    }

    private fun onCuatrimestreSeleccionado(nombreCuatrimestre: String) {
        binding.btnSelectMateria.visibility = View.VISIBLE
        binding.tvSelectedMateria.text = "Selecciona una materia"

        val cuatrimestre = allCuatrimestres.find { it.nombre == nombreCuatrimestre }
        materiasDelCuatrimestreSeleccionado = if (cuatrimestre != null) {
            allMaterias.filter { it.cuatrimestreId == cuatrimestre.id }
        } else {
            emptyList()
        }
        subjectsAdapter.submitList(materiasDelCuatrimestreSeleccionado)
    }

    private fun showSubjectsPopupMenu(view: View) {
        if (materiasDelCuatrimestreSeleccionado.isEmpty()) {
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = requireContext(),
                title = "Sin materias",
                message = "Este cuatrimestre todavía no tiene materias registradas.",
                type = com.classdrop.utils.AlertUtils.AlertType.WARNING
            )
            return
        }

        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)
        materiasDelCuatrimestreSeleccionado.forEachIndexed { index, materia ->
            popup.menu.add(0, index, index, materia.nombre)
        }

        popup.setOnMenuItemClickListener { item ->
            val seleccionada = materiasDelCuatrimestreSeleccionado[item.itemId]
            binding.tvSelectedMateria.text = seleccionada.nombre
            abrirDetalleMateria(seleccionada, seleccionada.nombre)
            true
        }
        popup.show()
    }

    private fun setupSubjectsAdapter() {
        subjectsAdapter = SubjectsAdapter { materia ->
            abrirDetalleMateria(materia, materia.nombre)
        }
        binding.rvSubjectsExplore.layoutManager = GridLayoutManager(context, 2)
        binding.rvSubjectsExplore.adapter = subjectsAdapter
    }

    private fun abrirDetalleMateria(materia: MateriaResponse?, nombreFallback: String) {
        val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
            putExtra("SUBJECT_ID", materia?.id)
            putExtra("SUBJECT_NAME", materia?.nombre ?: nombreFallback)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}