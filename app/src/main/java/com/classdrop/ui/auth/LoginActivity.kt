package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.R
import com.classdrop.databinding.ActivityLoginBinding
import com.classdrop.model.UserRole
import com.classdrop.network.NetworkResult
import com.classdrop.ui.admin.AdminHomeActivity
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.AuthViewModel
import com.classdrop.model.User

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Recuperar contraseña interna
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
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

    private fun observeViewModel() {
        viewModel.validationError.observe(this) { error ->
            error?.let {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Error de validación",
                    message = it,
                    type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                )
            }
        }

        viewModel.loginState.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> setLoading(true)
                is NetworkResult.Success -> {
                    setLoading(false)
                    val loginData = result.data
                    val user = loginData?.usuario
                    val role = user?.rol ?: UserRole.STUDENT
                    sessionManager.saveAuthToken(loginData?.token.orEmpty())
                    sessionManager.saveUserRole(role)
                    
                    // Preservar el nombre del registro si el servidor devuelve uno genérico
                    val serverName = user?.nombreCompleto ?: "Usuario"
                    val savedName = sessionManager.fetchUserName()
                    val finalName = if ((serverName == "Estudiante" || serverName == "Administrador") && savedName != "Usuario") {
                        savedName
                    } else {
                        serverName
                    }
                    
                    sessionManager.saveUserName(finalName)
                    sessionManager.saveUserEmail(user?.correo ?: "")
                    
                    com.classdrop.utils.AlertUtils.showCustomAlert(
                        context = this,
                        title = "¡Bienvenido!",
                        message = "Sesión iniciada correctamente",
                        type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS,
                        onPrimaryClick = { navigateByRole(role) }
                    )
                }
                is NetworkResult.Error -> {
                    setLoading(false)
                    com.classdrop.utils.AlertUtils.showCustomAlert(
                        context = this,
                        title = "Error de inicio de sesión",
                        message = result.message ?: "Credenciales incorrectas",
                        type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                    )
                }
            }
        }
    }

    private fun navigateByRole(role: UserRole) {
        val destination = if (role == UserRole.ADMIN) AdminHomeActivity::class.java else MainActivity::class.java
        startActivity(Intent(this, destination))
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}