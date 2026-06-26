package com.classdrop.model

data class LoginResponse(
    val token: String,
    val user: User
)
