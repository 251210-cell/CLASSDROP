package com.classdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.classdrop.R.layout.activity_splash)

        val sessionManager = SessionManager(this)
        val destination = if (sessionManager.fetchAuthToken() != null) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
    }
}