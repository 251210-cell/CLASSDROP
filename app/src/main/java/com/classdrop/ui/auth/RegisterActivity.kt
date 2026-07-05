package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.classdrop.R
import com.classdrop.databinding.ActivityRegisterBinding
import com.classdrop.network.NetworkResult
import com.classdrop.ui.profile.PrivacyPolicyActivity
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

        setupUI()
        setupListeners()
        setupObservers()
    }

    private fun setupUI() {
        setupTermsText()
    }

    private fun setupTermsText() {
        val fullText = "Acepto los Términos y Condiciones y la Política de Privacidad"
        val spannableString = SpannableString(fullText)

        // Índices para "Términos y Condiciones"
        val termsStart = fullText.indexOf("Términos y Condiciones")
        val termsEnd = termsStart + "Términos y Condiciones".length

        // Índices para "Política de Privacidad"
        val privacyStart = fullText.indexOf("Política de Privacidad")
        val privacyEnd = privacyStart + "Política de Privacidad".length

        val primaryColor = ContextCompat.getColor(this, R.color.primary)

        // Click para Términos y Condiciones
        val termsClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Puedes abrir PrivacyPolicy o una específica de términos si existe
                startActivity(Intent(this@RegisterActivity, PrivacyPolicyActivity::class.java))
            }
        }

        // Click para Política de Privacidad
        val privacyClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, PrivacyPolicyActivity::class.java))
            }
        }

        spannableString.setSpan(termsClick, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(primaryColor), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(privacyClick, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(primaryColor), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvTermsLink.text = spannableString
        binding.tvTermsLink.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupListeners() {
        // Toggle para ver/ocultar contraseña
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

            // VALIDACIÓN DE CHECKBOX
            if (!binding.cbTerms.isChecked) {
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "Aviso legal",
                    message = "Debes aceptar los Términos y Condiciones y la Política de Privacidad para registrarte.",
                    type = AlertUtils.AlertType.WARNING
                )
                return@setOnClickListener
            }

            // Desencadena el flujo en el ViewModel
            viewModel.register(name, email, password)
        }
    }

    private fun setupObservers() {
        // Observa errores de validación locales
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
                    showLoading(true)
                }
                is NetworkResult.Success -> {
                    showLoading(false)
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
                    showLoading(false)
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

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnRegister.visibility = View.INVISIBLE
            binding.pbLoading.visibility = View.VISIBLE
            binding.etName.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etPassword.isEnabled = false
            binding.cbTerms.isEnabled = false
        } else {
            binding.btnRegister.visibility = View.VISIBLE
            binding.pbLoading.visibility = View.GONE
            binding.etName.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPassword.isEnabled = true
            binding.cbTerms.isEnabled = true
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
