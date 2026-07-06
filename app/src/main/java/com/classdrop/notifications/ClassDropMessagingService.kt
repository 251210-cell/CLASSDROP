package com.classdrop.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ClassDropMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Nuevo token generado: $token")
        // Nota: Idealmente deberías llamar a tu API aquí también si el usuario ya está logueado
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Android muestra automáticamente la notificación si la app está en segundo plano.
        // Si está en primer plano (abierta), puedes manejarla aquí:
        remoteMessage.notification?.let {
            Log.d("FCM_MESSAGE", "Título: ${it.title}, Cuerpo: ${it.body}")
            // Aquí podrías mostrar un Toast o actualizar un contador en la UI
        }
    }
}