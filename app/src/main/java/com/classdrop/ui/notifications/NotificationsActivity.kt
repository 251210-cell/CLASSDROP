package com.classdrop.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityNotificationsBinding
import com.classdrop.repository.NotificationRepository

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

        adapter = NotificationsAdapter()
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
