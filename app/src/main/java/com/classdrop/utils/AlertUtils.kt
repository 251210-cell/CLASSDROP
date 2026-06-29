package com.classdrop.utils

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import com.classdrop.R
import com.classdrop.databinding.DialogCustomAlertBinding

object AlertUtils {

    enum class AlertType {
        SUCCESS, ERROR, CONFIRMATION, WARNING
    }

    fun showCustomAlert(
        context: Context,
        title: String,
        message: String,
        type: AlertType = AlertType.SUCCESS,
        primaryButtonText: String = "Continuar",
        secondaryButtonText: String? = null,
        showIcon: Boolean = true,
        onPrimaryClick: (() -> Unit)? = null,
        onSecondaryClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogCustomAlertBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Forzamos el ancho a 350dp para que todas sean igual de grandes ("totas")
        val width = (350 * context.resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(
            width,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.tvAlertTitle.text = title
        binding.tvAlertMessage.text = message
        binding.btnPrimary.text = primaryButtonText

        // Control de visibilidad del icono
        binding.flIconContainer.visibility = if (showIcon) View.VISIBLE else View.GONE

        if (showIcon) {
            val iconRes = when (type) {
                AlertType.SUCCESS -> R.drawable.ic_check_circle
                AlertType.ERROR -> R.drawable.ic_warning
                AlertType.WARNING -> R.drawable.ic_warning
                AlertType.CONFIRMATION -> R.drawable.ic_help
            }

            // Configuración según el tipo usando recursos de color para soporte de temas
            val color = when (type) {
                AlertType.SUCCESS -> ContextCompat.getColor(context, R.color.primary)
                AlertType.ERROR -> ContextCompat.getColor(context, R.color.error)
                AlertType.WARNING -> ContextCompat.getColor(context, R.color.warning)
                AlertType.CONFIRMATION -> ContextCompat.getColor(context, R.color.primary)
            }

            binding.ivAlertIcon.setImageResource(iconRes)
            binding.ivAlertIcon.imageTintList = ColorStateList.valueOf(color)
            binding.vIconBg.backgroundTintList = ColorStateList.valueOf(color)
            binding.tvAlertTitle.setTextColor(color)
            binding.btnPrimary.backgroundTintList = ColorStateList.valueOf(
                if (type == AlertType.ERROR) color else ContextCompat.getColor(context, R.color.primary)
            )
        } else {
            // Si no hay icono, usamos el color de la marca
            binding.tvAlertTitle.setTextColor(ContextCompat.getColor(context, R.color.on_background))
            binding.btnPrimary.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary))
        }

        // Botón secundario
        if (secondaryButtonText != null) {
            binding.btnSecondary.visibility = View.VISIBLE
            binding.btnSecondary.text = secondaryButtonText
            binding.btnSecondary.setOnClickListener {
                onSecondaryClick?.invoke()
                dialog.dismiss()
            }
        }

        binding.btnPrimary.setOnClickListener {
            onPrimaryClick?.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }
}
