package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
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
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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
                    
                    if (loginData?.requires2FA == true) {
                        val intent = Intent(this, VerifyOTPActivity::class.java).apply {
                            putExtra("USER_ID", loginData.userId)
                        }
                        startActivity(intent)
                        return@observe
                    }

                    val user = loginData?.usuario
                    val role = user?.rol ?: UserRole.STUDENT
                    sessionManager.saveAuthToken(loginData?.token.orEmpty())
                    sessionManager.saveUserRole(role)
                    
                    val serverName = user?.nombreCompleto ?: "Usuario"
                    sessionManager.saveUserName(serverName)
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
        val intent = Intent(this, destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }
}
