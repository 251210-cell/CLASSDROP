package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.domain.auth.ValidarCredencialesUseCase
import com.classdrop.model.*
import com.classdrop.network.AuthService
import com.classdrop.network.NetworkResult
import com.classdrop.network.RetrofitClient
import com.classdrop.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(
        RetrofitClient.create(application).create(AuthService::class.java)
    )
    private val validarCredenciales = ValidarCredencialesUseCase()

    private val _loginState = MutableLiveData<NetworkResult<LoginResponse>>()
    val loginState: LiveData<NetworkResult<LoginResponse>> = _loginState

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError

    private val _registerState = MutableLiveData<NetworkResult<RegisterResponse>>()
    val registerState: LiveData<NetworkResult<RegisterResponse>> = _registerState

    private val _verify2FAState = MutableLiveData<NetworkResult<LoginResponse>>()
    val verify2FAState: LiveData<NetworkResult<LoginResponse>> = _verify2FAState

    // Nuevos estados para activación de 2FA desde Perfil
    private val _generate2FAState = MutableLiveData<NetworkResult<String>>()
    val generate2FAState: LiveData<NetworkResult<String>> = _generate2FAState

    private val _activate2FAState = MutableLiveData<NetworkResult<String>>()
    val activate2FAState: LiveData<NetworkResult<String>> = _activate2FAState

    fun login(correo: String, contrsena: String) {
        when (val resultado = validarCredenciales(correo, contrsena)) {
            is ValidarCredencialesUseCase.Resultado.Invalido -> {
                _validationError.value = resultado.mensaje
            }
            ValidarCredencialesUseCase.Resultado.Valido -> {
                _validationError.value = null
                _loginState.value = NetworkResult.Loading()
                viewModelScope.launch {
                    _loginState.value = authRepository.login(correo.trim(), contrsena)
                }
            }
        }
    }

    fun verifyCode(userId: String, code: String) {
        if (code.length < 6) {
            _validationError.value = "El código debe ser de 6 dígitos"
            return
        }
        _verify2FAState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _verify2FAState.value = authRepository.verify2FA(userId, code)
        }
    }

    // --- ACTIVACIÓN DESDE PERFIL ---

    fun generate2FACode() {
        _generate2FAState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _generate2FAState.value = authRepository.generar2FACodigo()
        }
    }

    fun activate2FA(code: String) {
        if (code.length < 6) {
            _validationError.value = "Ingresa los 6 dígitos"
            return
        }
        _activate2FAState.value = NetworkResult.Loading()
        viewModelScope.launch {
            _activate2FAState.value = authRepository.activar2FA(code)
        }
    }

    fun register(nombre: String, correo: String, contrasena: String) {
        when (val resultado = validarCredenciales(correo, contrasena)) {
            is ValidarCredencialesUseCase.Resultado.Invalido -> {
                _validationError.value = resultado.mensaje
            }
            ValidarCredencialesUseCase.Resultado.Valido -> {
                _validationError.value = null
                _registerState.value = NetworkResult.Loading()
                viewModelScope.launch {
                    _registerState.value = authRepository.register(nombre, correo, contrasena)
                }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
