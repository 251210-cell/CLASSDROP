package com.classdrop.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityNotificationsBinding
import com.classdrop.model.NotificationType
import com.classdrop.repository.NotificationRepository
import com.classdrop.ui.files.FileRejectedActivity
import com.classdrop.ui.files.FileSuccessActivity

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeNotifications()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adapter = NotificationsAdapter { notification ->
            when (notification.type) {
                NotificationType.SUCCESS -> {
                    // Si es éxito de publicación, mostrar pantalla de éxito
                    if (notification.title.contains("Archivo Publicado", ignoreCase = true)) {
                        startActivity(Intent(this, FileSuccessActivity::class.java))
                    }
                }
                NotificationType.ERROR -> {
                    // Si es error de publicación, mostrar pantalla de rechazo
                    if (notification.title.contains("Archivo Rechazado", ignoreCase = true)) {
                        startActivity(Intent(this, FileRejectedActivity::class.java))
                    }
                }
                else -> {
                    // Otras notificaciones
                }
            }
        }
        
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun observeNotifications() {
        NotificationRepository.notifications.observe(this) { notifications ->
            if (notifications.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvNotifications.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvNotifications.visibility = View.VISIBLE
                adapter.submitList(notifications)
            }
        }
    }
}
