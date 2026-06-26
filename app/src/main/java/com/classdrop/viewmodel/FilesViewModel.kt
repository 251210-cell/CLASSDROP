package com.classdrop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.classdrop.model.Comment
import java.util.UUID

class FilesViewModel : ViewModel() {

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    init {
        loadMockComments()
    }

    private fun loadMockComments() {
        val mockComments = listOf(
            Comment("1", "user1", "¡Excelente material! Me sirvió mucho para el examen.", System.currentTimeMillis() - 3600000),
            Comment("2", "user2", "Faltan un par de fórmulas en la segunda página, pero está muy completo.", System.currentTimeMillis() - 7200000)
        )
        _comments.value = mockComments
    }

    fun addComment(content: String) {
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            userId = "currentUser",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val currentList = _comments.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, newComment)
        _comments.value = currentList
    }
}
