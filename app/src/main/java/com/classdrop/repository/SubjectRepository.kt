package com.classdrop.repository

import android.content.Context
import com.classdrop.R
import com.classdrop.data.local.AppDatabase
import com.classdrop.data.local.dao.SubjectDao
import com.classdrop.data.mapper.toDomain
import com.classdrop.data.mapper.toEntity
import com.classdrop.model.Subject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubjectRepository(context: Context) {
    private val subjectDao: SubjectDao = AppDatabase.getDatabase(context).subjectDao()

    fun getAllSubjects(): Flow<List<Subject>> {
        return subjectDao.getAllSubjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addSubject(subject: Subject) {
        subjectDao.insertSubject(subject.toEntity())
    }

    suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject.toEntity())
    }

    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject.toEntity())
    }

    suspend fun getSubjectById(id: String): Subject? {
        return subjectDao.getSubjectById(id)?.toDomain()
    }

    // Static initialization for first-time use - Now empty to allow real data population
    suspend fun initializeDefaultSubjects() {
        // No hardcoded subjects to allow database to be populated from real sources
    }
}
