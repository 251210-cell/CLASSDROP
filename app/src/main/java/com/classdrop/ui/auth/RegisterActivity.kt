package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.R
import com.classdrop.databinding.ActivityRegisterBinding
import com.classdrop.network.NetworkResult
import com.classdrop.viewmodel.AuthViewModel
import com.classdrop.utils.AlertUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        // Toggle para ver/ocultar contraseña (mantenemos tu lógica original)
        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Volver al Login
        binding.tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Botón de Registro conectado al ViewModel
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (name.isEmpty()) {
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "Campo requerido",
                    message = "Por favor ingresa tu nombre completo",
                    type = AlertUtils.AlertType.WARNING
                )
                return@setOnClickListener
            }

            // Desencadena el flujo en el ViewModel (este validará con el UseCase)
            viewModel.register(name, email, password)
        }
    }

    private fun setupObservers() {
        // Observa errores de validación locales (del UseCase) usando tus alertas
        viewModel.validationError.observe(this) { errorMessage ->
            errorMessage?.let {
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "Datos inválidos",
                    message = it,
                    type = AlertUtils.AlertType.ERROR
                )
            }
        }

        // Observa la respuesta del servidor
        viewModel.registerState.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.btnRegister.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.btnRegister.isEnabled = true

                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "¡Registro Exitoso!",
                        message = "Tu cuenta ha sido creada. Ahora puedes iniciar sesión.",
                        type = AlertUtils.AlertType.SUCCESS,
                        onPrimaryClick = {
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    )
                }
                is NetworkResult.Error -> {
                    binding.btnRegister.isEnabled = true
                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "Error de Registro",
                        message = result.message ?: "Error desconocido",
                        type = AlertUtils.AlertType.ERROR
                    )
                }
            }
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_hide)
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_show)
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }
}