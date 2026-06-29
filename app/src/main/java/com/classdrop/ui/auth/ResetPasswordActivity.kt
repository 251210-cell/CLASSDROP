package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.databinding.ActivityResetPasswordBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpdatePassword.setOnClickListener {
            val code = binding.etCode.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (code.isEmpty() || newPassword.length < 8 || newPassword != confirmPassword) {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "Error de Validación",
                    message = "Por favor verifica que el código sea correcto y que las contraseñas coincidan (mín. 8 caracteres).",
                    type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                )
                return@setOnClickListener
            }

            // Simulación de éxito en la API
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = this,
                title = "¡Actualizado!",
                message = "Tu contraseña ha sido actualizada exitosamente.",
                type = com.classdrop.utils.AlertUtils.AlertType.SUCCESS,
                onPrimaryClick = {
                    // Guardar sesión para entrar directamente
                    sessionManager.saveAuthToken("simulated_token")
                    sessionManager.saveUserName("Usuario ClassDrop")
                    
                    // Navegar directamente al Inicio (MainActivity)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}