
package com.classdrop.model

data class ApiError(
    val code: String?,
    val message: String?,
    val details: Any? // Puedes usar Any? por si mandas listas de detalles
)