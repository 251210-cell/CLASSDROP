package com.classdrop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.classdrop.model.Subject
import com.classdrop.repository.SubjectRepository

class SubjectsViewModel : ViewModel() {
    val subjects: LiveData<List<Subject>> = SubjectRepository.subjects
}
