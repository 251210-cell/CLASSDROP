package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.Subject
import com.classdrop.repository.SubjectRepository
import kotlinx.coroutines.launch

class SubjectsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SubjectRepository(application)
    val subjects: LiveData<List<Subject>> = repository.getAllSubjects().asLiveData()

    init {
        viewModelScope.launch {
            // Check if we need to initialize
            val current = repository.getSubjectById("1")
            if (current == null) {
                repository.initializeDefaultSubjects()
            }
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }
}
