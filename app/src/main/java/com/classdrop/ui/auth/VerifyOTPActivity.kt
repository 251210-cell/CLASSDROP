package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityVerifyOtpBinding
import com.classdrop.model.UserRole
import com.classdrop.network.NetworkResult
import com.classdrop.ui.admin.AdminHomeActivity
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.AuthViewModel

class VerifyOTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyOtpBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        userId = intent.getStringExtra("USER_ID")

        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnVerify.setOnClickListener {
            val code = binding.etOtpCode.text.toString().trim()
            userId?.let { id ->
                viewModel.verifyCode(id, code)
            }
        }
    }

    private fun setupObservers() {
        viewModel.validationError.observe(this) { error ->
            error?.let {
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "Código inválido",
                    message = it,
                    type = AlertUtils.AlertType.ERROR
                )
            }
        }

        viewModel.verify2FAState.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> showLoading(true)
                is NetworkResult.Success -> {
                    showLoading(false)
                    val loginData = result.data
                    val user = loginData?.usuario
                    val role = user?.rol ?: UserRole.STUDENT
                    
                    // Guardar sesión final
                    sessionManager.saveAuthToken(loginData?.token.orEmpty())
                    sessionManager.saveUserRole(role)
                    sessionManager.saveUserName(user?.nombreCompleto ?: "Usuario")
                    sessionManager.saveUserEmail(user?.correo ?: "")

                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "¡Verificado!",
                        message = "Tu identidad ha sido confirmada.",
                        type = AlertUtils.AlertType.SUCCESS,
                        onPrimaryClick = { navigateByRole(role) }
                    )
                }
                is NetworkResult.Error -> {
                    showLoading(false)
                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "Error de verificación",
                        message = result.message ?: "Código incorrecto",
                        type = AlertUtils.AlertType.ERROR
                    )
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnVerify.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.etOtpCode.isEnabled = !isLoading
    }

    private fun navigateByRole(role: UserRole) {
        val destination = if (role == UserRole.ADMIN) AdminHomeActivity::class.java else MainActivity::class.java
        val intent = Intent(this, destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
