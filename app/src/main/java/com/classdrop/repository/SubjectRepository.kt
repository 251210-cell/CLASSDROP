package com.classdrop.repository

import com.classdrop.R
import com.classdrop.model.Subject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SubjectRepository {
    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> = _subjects

    init {
        _subjects.value = listOf(
            Subject("1", "Cálculo Integral", 12, R.drawable.ic_mortarboard, "#EEF2FF", "#4F46E5", "1 Cuatrimestre"),
            Subject("2", "Algoritmos Avanzados", 8, R.drawable.ic_subject_code, "#F0FDFA", "#0D9488", "7 Cuatrimestre"),
            Subject("3", "Ética Profesional", 15, R.drawable.ic_mortarboard, "#F5F3FF", "#8B5CF6", "3 Cuatrimestre"),
            Subject("4", "Bases de Datos I", 20, R.drawable.ic_database, "#EFF6FF", "#3B82F6", "3 Cuatrimestre")
        )
    }

    fun addSubject(subject: Subject) {
        val currentList = _subjects.value?.toMutableList() ?: mutableListOf()
        currentList.add(subject)
        _subjects.value = currentList
    }

    fun updateSubject(updatedSubject: Subject) {
        val currentList = _subjects.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == updatedSubject.id }
        if (index != -1) {
            currentList[index] = updatedSubject
            _subjects.value = currentList
        }
    }

    fun deleteSubject(subjectId: String) {
        val currentList = _subjects.value?.toMutableList() ?: return
        currentList.removeAll { it.id == subjectId }
        _subjects.value = currentList
    }
    
    fun getSubjectById(id: String): Subject? {
        return _subjects.value?.find { it.id == id }
    }
}
