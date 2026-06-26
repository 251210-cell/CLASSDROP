package com.classdrop.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.classdrop.databinding.ActivityMainBinding
import com.classdrop.ui.explore.ExploreFragment
import com.classdrop.ui.files.UploadFileActivity
import com.classdrop.ui.home.HomeFragment
import com.classdrop.ui.profile.ProfileFragment

/**
 * Contenedor principal de la app. Hospeda los 3 destinos permanentes del
 * BottomNavBar (Inicio, Explorar, Perfil) como Fragments intercambiables.
 * "Subir archivo" no es un tab persistente: abre UploadFileActivity encima
 * y no cambia la selección visual de la barra al volver.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment by lazy { HomeFragment() }
    private val exploreFragment by lazy { ExploreFragment() }
    private val profileFragment by lazy { ProfileFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()

        if (savedInstanceState == null) {
            selectTab(Tab.HOME)
        }
    }

    private fun setupBottomNav() {
        val nav = binding.includeBottomNav
        nav.btnNavHome.setOnClickListener { selectTab(Tab.HOME) }
        nav.btnNavSearch.setOnClickListener { selectTab(Tab.EXPLORE) }
        nav.btnNavProfile.setOnClickListener { selectTab(Tab.PROFILE) }
        nav.btnNavUpload.setOnClickListener {
            startActivity(Intent(this, UploadFileActivity::class.java))
        }
    }

    private fun selectTab(tab: Tab) {
        val fragment: Fragment = when (tab) {
            Tab.HOME -> homeFragment
            Tab.EXPLORE -> exploreFragment
            Tab.PROFILE -> profileFragment
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()

        updateNavIconTint(tab)
    }

    /** Pinta de @color/primary el ícono activo y de colorOnSurfaceVariant el resto. */
    private fun updateNavIconTint(selected: Tab) {
        val nav = binding.includeBottomNav
        val icons = mapOf(
            Tab.HOME to (nav.btnNavHome.getChildAt(0) as android.widget.ImageView),
            Tab.EXPLORE to (nav.btnNavSearch.getChildAt(0) as android.widget.ImageView),
            Tab.PROFILE to (nav.btnNavProfile.getChildAt(0) as android.widget.ImageView)
        )
        val activeColor = com.google.android.material.color.MaterialColors.getColor(
            binding.root, com.google.android.material.R.attr.colorPrimary
        )
        val inactiveColor = com.google.android.material.color.MaterialColors.getColor(
            binding.root, com.google.android.material.R.attr.colorOnSurfaceVariant
        )
        icons.forEach { (tab, imageView) ->
            imageView.setColorFilter(if (tab == selected) activeColor else inactiveColor)
        }
    }

    private enum class Tab { HOME, EXPLORE, PROFILE }
}