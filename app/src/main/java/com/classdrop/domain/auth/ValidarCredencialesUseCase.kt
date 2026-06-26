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
        if (password.isBlank()) {
            return Resultado.Invalido("La contraseña no puede estar vacía")
        }
        if (password.length < 6) {
            return Resultado.Invalido("La contraseña debe tener al menos 6 caracteres")
        }
        return Resultado.Valido
    }
}
