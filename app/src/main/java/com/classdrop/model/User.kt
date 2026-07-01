package com.classdrop.model

import com.google.gson.annotations.SerializedName
data class User(
    val id: String,
    val nombreCompleto: String,
    val correo: String,
    val rol: UserRole
)

/**
 * Rol del usuario, tal como lo devuelve el backend en minúsculas
 * (ej. "student" o "admin"). @SerializedName evita depender de que
 * el backend use exactamente "STUDENT"/"ADMIN" en mayúsculas.
 */
enum class UserRole {
    @SerializedName("estudiante")
    STUDENT,
    @SerializedName("admin")
    ADMIN
}

