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
            Subject("1", "Cálculo II", 12, R.drawable.ic_nav_search, "#F3E8FF", "#A855F7"),
            Subject("2", "Programación", 8, R.drawable.ic_nav_upload, "#E0F2F1", "#2DD4BF"),
            Subject("3", "Base de Datos", 15, R.drawable.ic_nav_profile, "#E0E7FF", "#6366F1"),
            Subject("4", "Álgebra", 4, R.drawable.ic_nav_home, "#FFE4E6", "#F43F5E")
        )
    }
}
