package com.classdrop.network

import android.content.Context
import com.classdrop.utils.Constants
import com.classdrop.utils.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    fun create(context: Context): Retrofit {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        val sessionManager = SessionManager(context)
        
        // CONFIGURACIÓN DE RED PARA RAILWAY (TIEMPOS LARGOS PARA ENVÍO DE MAIL)
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .connectTimeout(120, TimeUnit.SECONDS) // 2 minutos para conectar (por si el server arranca)
            .readTimeout(120, TimeUnit.SECONDS)    // 2 minutos para esperar el envío del mail
            .writeTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
