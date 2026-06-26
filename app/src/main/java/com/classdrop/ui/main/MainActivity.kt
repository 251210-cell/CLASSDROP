package com.classdrop.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.classdrop.databinding.ActivityMainBinding
import com.classdrop.ui.explore.ExploreFragment
import com.classdrop.ui.files.UploadFileActivity
import com.classdrop.ui.files.FileStatusFragment
import com.classdrop.ui.home.HomeFragment
import com.classdrop.ui.profile.ProfileFragment

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

    enum class Tab { HOME, EXPLORE, STATUS, PROFILE }
}