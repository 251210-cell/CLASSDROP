package com.classdrop.model

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val meta: Any?,
    val error: String?
)