package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.R
import com.classdrop.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Toggle para ver/ocultar contraseña
        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            val validator = com.classdrop.domain.auth.ValidarCredencialesUseCase()
            val validationResult = validator(email, password)

            if (name.isEmpty()) {
                android.widget.Toast.makeText(this, "Por favor ingresa tu nombre", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validationResult is com.classdrop.domain.auth.ValidarCredencialesUseCase.Resultado.Invalido) {
                android.widget.Toast.makeText(this, validationResult.mensaje, android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulación: Guardamos los datos para que el Perfil los use después del Login
            val sessionManager = com.classdrop.utils.SessionManager(this)
            sessionManager.saveUserName(name)
            sessionManager.saveUserEmail(email)
            
            android.widget.Toast.makeText(this, "Registro exitoso. Por favor inicia sesión.", android.widget.Toast.LENGTH_LONG).show()
            
            // Redirigir al Login en lugar de entrar directamente
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Volver al Login
        binding.tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
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