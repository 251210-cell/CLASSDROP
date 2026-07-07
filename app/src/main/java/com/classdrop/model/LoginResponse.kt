package com.classdrop.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val token: String? = null,
    val usuario: User? = null,
    
    // CAMPOS PARA 2FA (Sincronizados con auth.controller.js)
    @SerializedName("requires2FA") val requires2FA: Boolean = false,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("mensaje") val mensaje: String? = null
)
