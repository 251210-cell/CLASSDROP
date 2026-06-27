package com.classdrop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.classdrop.R
import com.classdrop.model.Subject

class SubjectsViewModel : ViewModel() {
    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> = _subjects

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        _subjects.value = listOf(
            Subject("1", "Cálculo Integral", 12, R.drawable.ic_mortarboard, "#EEF2FF", "#4F46E5", "1 Cuatrimestre"),
            Subject("2", "Algoritmos Avanzados", 8, R.drawable.ic_nav_notes, "#F0FDFA", "#0D9488", "7 Cuatrimestre"),
            Subject("3", "Ética Profesional", 15, R.drawable.ic_mortarboard, "#F5F3FF", "#8B5CF6", "3 Cuatrimestre"),
            Subject("4", "Bases de Datos I", 20, R.drawable.ic_nav_notes, "#EFF6FF", "#3B82F6", "3 Cuatrimestre")
        )
    }
}
