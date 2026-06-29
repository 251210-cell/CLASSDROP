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

    // Static initialization for first-time use
    suspend fun initializeDefaultSubjects() {
        val defaults = listOf(
            Subject("1", "Cálculo Integral", 12, R.drawable.ic_mortarboard, "#EEF2FF", "#4F46E5", "1 Cuatrimestre"),
            Subject("2", "Algoritmos Avanzados", 8, R.drawable.ic_subject_code, "#F0FDFA", "#0D9488", "7 Cuatrimestre"),
            Subject("3", "Ética Profesional", 15, R.drawable.ic_mortarboard, "#F5F3FF", "#8B5CF6", "3 Cuatrimestre"),
            Subject("4", "Bases de Datos I", 20, R.drawable.ic_database, "#EFF6FF", "#3B82F6", "3 Cuatrimestre")
        )
        defaults.forEach { addSubject(it) }
    }
}
