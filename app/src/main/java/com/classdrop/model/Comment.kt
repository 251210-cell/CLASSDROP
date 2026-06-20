package com.classdrop.model

data class Comment(
    val id: String,
    val userId: String,
    val content: String,
    val timestamp: Long
)
