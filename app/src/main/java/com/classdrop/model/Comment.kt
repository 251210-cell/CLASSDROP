package com.classdrop.model

data class Comment(
    val id: String,
    val userId: String,
    val content: String,
    val timestamp: Long,
    var likes: Int = 0,
    var dislikes: Int = 0,
    var isLiked: Boolean = false,
    var isDisliked: Boolean = false
)
