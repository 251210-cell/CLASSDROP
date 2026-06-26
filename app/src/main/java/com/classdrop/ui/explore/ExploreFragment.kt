package com.classdrop.ui.explore

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.FragmentExploreBinding
import com.classdrop.model.Subject
import com.classdrop.ui.home.SubjectsAdapter

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var subjectsAdapter: SubjectsAdapter
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    private var allSubjects: List<Subject> = emptyList()

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
        setupCuatrimestres()
        setupSubjects()
        setupSuggestions()
        setupSearch()
    }

    private fun setupSuggestions() {
        suggestionsAdapter = SuggestionsAdapter { suggestion ->
            binding.etSearch.setText(suggestion.title)
            binding.rvSuggestions.visibility = View.GONE
            
            // Navegar al detalle
            val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
                putExtra("SUBJECT_NAME", suggestion.title)
                putExtra("FILE_COUNT", (5..20).random())
            }
            startActivity(intent)
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
                    subjectsAdapter.submitList(allSubjects)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showSuggestions(query: String) {
        val suggestions = allSubjects
            .filter { it.name.contains(query, ignoreCase = true) }
            .map { Suggestion(it.name, "Materia") }
        
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
            allSubjects
        } else {
            allSubjects.filter { 
                it.name.contains(query, ignoreCase = true)
            }
        }
        subjectsAdapter.submitList(filteredList)
    }

    private fun setupCuatrimestres() {
        val quarters = (1..10).map { it.toString() }
        binding.rvCuatrimestres.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCuatrimestres.adapter = QuartersAdapter(quarters) { quarter ->
            // Al seleccionar un cuatrimestre, mostramos el dropdown de materias
            binding.btnSelectMateria.visibility = View.VISIBLE
            binding.tvSelectedMateria.text = "Selecciona una materia"
            
            // Filtramos la lista principal por cuatrimestre (Simulado)
            filterByQuarter(quarter)
        }

        binding.btnSelectMateria.setOnClickListener {
            showSubjectsPopupMenu(it)
        }
    }

    private fun filterByQuarter(quarter: String) {
        // En una app real esto vendría de una DB o API
        val filtered = allSubjects.shuffled().take(2) 
        subjectsAdapter.submitList(filtered)
    }

    private fun showSubjectsPopupMenu(view: View) {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)
        val subjects = listOf("Cálculo II", "Programación", "Base de Datos", "Álgebra")
        
        subjects.forEachIndexed { index, name ->
            popup.menu.add(0, index, index, name)
        }

        popup.setOnMenuItemClickListener { item ->
            val selectedName = subjects[item.itemId]
            binding.tvSelectedMateria.text = selectedName
            
            // Navegar al detalle
            val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
                putExtra("SUBJECT_NAME", selectedName)
                putExtra("FILE_COUNT", (5..20).random()) 
            }
            startActivity(intent)
            true
        }
        popup.show()
    }

    private fun setupSubjects() {
        allSubjects = listOf(
            Subject("1", "Cálculo II", 12, R.drawable.ic_app_logo, "#E0E7FF", "#4F46E5"),
            Subject("2", "Programación", 8, R.drawable.ic_app_logo, "#ECFDF5", "#059669"),
            Subject("3", "Base de Datos", 15, R.drawable.ic_app_logo, "#F5F3FF", "#7C3AED"),
            Subject("4", "Álgebra", 4, R.drawable.ic_app_logo, "#FEF2F2", "#DC2626")
        )

        subjectsAdapter = SubjectsAdapter { subject ->
            val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
                putExtra("SUBJECT_NAME", subject.name)
                putExtra("FILE_COUNT", subject.fileCount)
            }
            startActivity(intent)
        }

        binding.rvSubjectsExplore.layoutManager = GridLayoutManager(context, 2)
        binding.rvSubjectsExplore.adapter = subjectsAdapter
        subjectsAdapter.submitList(allSubjects)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}