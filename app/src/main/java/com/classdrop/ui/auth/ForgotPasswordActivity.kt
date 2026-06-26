package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isNotEmpty()) {
                // Simulamos el envío y pasamos a la siguiente pantalla interna
                Toast.makeText(this, "Código enviado a $email", Toast.LENGTH_SHORT).show()
                
                val intent = Intent(this, ResetPasswordActivity::class.java)
                intent.putExtra("USER_EMAIL", email)
                startActivity(intent)
                finish() // Cerramos esta para que no regrese aquí al dar atrás
            } else {
                Toast.makeText(this, "Por favor, ingresa tu correo institucional", Toast.LENGTH_SHORT).show()
            }
        }
    }
}