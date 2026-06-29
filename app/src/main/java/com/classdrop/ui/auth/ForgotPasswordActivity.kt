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
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Código Enviado",
                    message = "Se ha enviado un código de verificación a $email",
                    type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS,
                    onPrimaryClick = {
                        val intent = Intent(this, ResetPasswordActivity::class.java)
                        intent.putExtra("USER_EMAIL", email)
                        startActivity(intent)
                        finish()
                    }
                )
            } else {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Campo Requerido",
                    message = "Por favor, ingresa tu correo institucional",
                    type = com.classdrop.utils.AlertUtils.AlertType.WARNING
                )
            }
        }
    }
}