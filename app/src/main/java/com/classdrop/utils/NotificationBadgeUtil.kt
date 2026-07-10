package com.classdrop.utils

import android.content.Context
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.classdrop.R
import com.classdrop.network.NetworkResult
import com.classdrop.repository.NotificationsApiRepository
import kotlinx.coroutines.launch

/**
 * Punto rojo de "tienes notificaciones sin leer" para el ícono de campana.
 * Se dibuja con el "overlay" de la View (icono.overlay.add(...)), así que
 * NO hace falta envolver el ImageView en un FrameLayout en cada XML: se
 * pinta encima del ícono tal cual ya está en cada pantalla.
 *
 * Uso (una sola línea, típicamente en onResume()):
 *   NotificationBadgeUtil.actualizar(requireContext(), lifecycleScope, binding.ivNotification)
 */
object NotificationBadgeUtil {

    private const val TAMANO_DP = 10f

    fun actualizar(context: Context, scope: LifecycleCoroutineScope, vararg iconos: ImageView) {
        scope.launch {
            val repository = NotificationsApiRepository(context)
            val resultado = repository.obtenerContadorNoLeidas()
            val hayNoLeidas = (resultado as? NetworkResult.Success)?.data?.let { it > 0 } ?: false
            iconos.forEach { icono -> aplicar(icono, hayNoLeidas) }
        }
    }

    private fun aplicar(icono: ImageView, mostrar: Boolean) {
        icono.overlay.clear()
        if (!mostrar) return

        val punto = ContextCompat.getDrawable(icono.context, R.drawable.dot_notification_badge) ?: return
        val tamanoPx = (TAMANO_DP * icono.context.resources.displayMetrics.density).toInt()

        // post{} para asegurarnos de que el ícono ya tiene ancho medido antes
        // de calcular la esquina superior derecha.
        icono.post {
            val ancho = if (icono.width > 0) icono.width else icono.layoutParams?.width ?: tamanoPx
            punto.setBounds(ancho - tamanoPx, 0, ancho, tamanoPx)
            icono.overlay.add(punto)
        }
    }

    fun actualizarDot(context: Context, scope: LifecycleCoroutineScope, dot: android.view.View) {
        scope.launch {
            val repository = NotificationsApiRepository(context)
            val resultado = repository.obtenerContadorNoLeidas()
            val hayNoLeidas = (resultado as? NetworkResult.Success)?.data?.let { it > 0 } ?: false
            dot.visibility = if (hayNoLeidas) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}