package com.classdrop.ui.main

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.classdrop.databinding.ActivityMainBinding
import com.classdrop.model.FcmTokenRequest
import com.classdrop.network.RetrofitClient
import com.classdrop.ui.explore.ExploreFragment
import com.classdrop.ui.files.UploadFileActivity
import com.classdrop.ui.files.FileStatusFragment
import com.classdrop.ui.home.HomeFragment
import com.classdrop.ui.profile.ProfileFragment
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment by lazy { HomeFragment() }
    private val exploreFragment by lazy { ExploreFragment() }
    private val profileFragment by lazy { ProfileFragment() }
    private val fileStatusFragment by lazy { FileStatusFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()
        handleIntent(intent)

        if (savedInstanceState == null && intent.getStringExtra("SELECT_TAB") == null) {
            selectTab(Tab.HOME)
        }

        // 1. LLAMAMOS A LA FUNCIÓN AQUÍ, AL INICIAR LA ACTIVIDAD PRINCIPAL
        registrarTokenFCM()

        checkNotificationPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("SELECT_TAB")?.let {
            val tab = when (it) {
                "PROFILE" -> Tab.PROFILE
                "STATUS" -> Tab.STATUS
                else -> Tab.HOME
            }

            if (tab == Tab.STATUS) {
                val fileName = intent.getStringExtra("FILE_NAME")
                val fileSize = intent.getStringExtra("FILE_SIZE")
                fileStatusFragment.arguments = Bundle().apply {
                    putString("FILE_NAME", fileName)
                    putString("FILE_SIZE", fileSize)
                }
            }

            selectTab(tab)
        }
    }

    private fun setupBottomNav() {
        val nav = binding.includeBottomNav
        nav.btnNavHome.setOnClickListener { selectTab(Tab.HOME) }
        nav.btnNavSearch.setOnClickListener { selectTab(Tab.EXPLORE) }
        nav.btnNavNotes.setOnClickListener { selectTab(Tab.STATUS) }
        nav.btnNavUpload.setOnClickListener {
            startActivity(Intent(this, UploadFileActivity::class.java))
        }
    }

    fun selectTab(tab: Tab) {
        val fragment: Fragment = when (tab) {
            Tab.HOME -> homeFragment
            Tab.EXPLORE -> exploreFragment
            Tab.STATUS -> fileStatusFragment
            Tab.PROFILE -> profileFragment
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()

        updateNavIconTint(tab)
    }

    private fun updateNavIconTint(selected: Tab) {
        val nav = binding.includeBottomNav
        val icons = mutableMapOf(
            Tab.HOME to (nav.btnNavHome.getChildAt(0) as android.widget.ImageView),
            Tab.EXPLORE to (nav.btnNavSearch.getChildAt(0) as android.widget.ImageView),
            Tab.STATUS to (nav.btnNavNotes.getChildAt(0) as android.widget.ImageView)
        )

        val activeColor = android.graphics.Color.parseColor("#6366F1")
        val inactiveColor = android.graphics.Color.parseColor("#94A3B8")

        icons.forEach { (tab, imageView) ->
            imageView.setColorFilter(if (tab == selected) activeColor else inactiveColor)
        }
    }

    // 2. AGREGAMOS LA FUNCIÓN PARA OBTENER Y ENVIAR EL TOKEN AL BACKEND
    private fun registrarTokenFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "Token obtenido de Firebase: $token")

                lifecycleScope.launch {
                    try {
                        val retrofit = RetrofitClient.create(this@MainActivity)
                        val authService = retrofit.create(com.classdrop.network.AuthService::class.java)

                        // Guardamos la respuesta del servidor
                        val response = authService.updateFcmToken(FcmTokenRequest(token))

                        // VERIFICAMOS SI REALMENTE FUE EXITOSA LA PETICIÓN HTTP
                        if (response.isSuccessful) {
                            Log.d("FCM_TOKEN", "✅ ¡El Backend guardó el token con éxito!")
                        } else {
                            // Esto te dirá el código exacto (401, 404, 500) y el motivo del fallo
                            val errorBody = response.errorBody()?.string()
                            Log.e("FCM_TOKEN", "❌ El Backend rechazó el token. Código: ${response.code()} | Error: $errorBody")
                        }
                    } catch (e: Exception) {
                        Log.e("FCM_TOKEN", "💥 Error de red o conexión al enviar al backend", e)
                    }
                }
            } else {
                Log.e("FCM_TOKEN", "No se pudo obtener el token desde Firebase Console")
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("PERMISO", "Permiso de notificaciones concedido")
            registrarTokenFCM() // Ahora sí obtenemos el token
        } else {
            Log.d("PERMISO", "El usuario rechazó las notificaciones")
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // En versiones anteriores a Android 13 no es necesario pedir permiso
            registrarTokenFCM()
        }
    }

    enum class Tab { HOME, EXPLORE, STATUS, PROFILE }
}