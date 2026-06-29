package com.classdrop.domain.auth

import android.util.Patterns

class ValidarCredencialesUseCase {

    sealed class Resultado {
        object Valido : Resultado()
        data class Invalido(val mensaje: String) : Resultado()
    }

    operator fun invoke(email: String, password: String): Resultado {
        if (email.isBlank()) {
            return Resultado.Invalido("El correo electrónico no puede estar vacío")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resultado.Invalido("El formato del correo electrónico no es válido")
        }
        
        val allowedDomains = listOf("@it2id.upchiapas.edu.mx", "@ids.upchiapas.edu.mx", "@classdrop.com")
        if (allowedDomains.none { email.endsWith(it, ignoreCase = true) }) {
            return Resultado.Invalido("El dominio del correo no está autorizado")
        }

        if (password.isBlank()) {
            return Resultado.Invalido("La contraseña no puede estar vacía")
        }
        if (password.length < 8 || password.length > 20) {
            return Resultado.Invalido("La contraseña debe tener entre 8 y 20 caracteres")
        }
        if (!password.all { it.isLetterOrDigit() }) {
            return Resultado.Invalido("La contraseña no debe contener símbolos ni caracteres especiales")
        }
        return Resultado.Valido
    }
}
