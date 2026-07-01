package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.domain.auth.ValidarCredencialesUseCase
import com.classdrop.model.LoginResponse
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
}