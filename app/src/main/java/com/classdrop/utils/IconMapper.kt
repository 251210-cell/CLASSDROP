package com.classdrop.utils

import com.classdrop.R

/**
 * Fuente única de verdad para los íconos de materia.
 */
object IconMapper {

    data class IconStyle(
        val key: String,
        val drawableRes: Int,
        val bgColor: String,
        val tintColor: String
    )

    private val ICONS = listOf(
        IconStyle("code", R.drawable.ic_subject_code, "#F5F3FF", "#7C3AED"),
        IconStyle("database", R.drawable.ic_database, "#EFF6FF", "#2563EB"),
        IconStyle("structure", R.drawable.ic_subject_structure, "#FFF1F2", "#E11D48"),
        IconStyle("network", R.drawable.ic_subject_network, "#F0F9FF", "#0369A1"),
        IconStyle("ai", R.drawable.ic_subject_ai, "#F0FDF4", "#16A34A"),
        IconStyle("analytics", R.drawable.ic_subject_analytics, "#FEFCE8", "#CA8A04"),
        IconStyle("language", R.drawable.ic_subject_language, "#FAF5FF", "#9333EA"),
        IconStyle("android", R.drawable.ic_subject_android, "#F0FDF4", "#15803D"),
        IconStyle("cooperation", R.drawable.ic_subject_cooperation, "#FFF1F2", "#BE123C"),
        IconStyle("hardware", R.drawable.ic_subject_hardware, "#F8FAFC", "#475569")
    )

    private val DEFAULT = IconStyle("default", R.drawable.ic_mortarboard, "#EEF2FF", "#4F46E5")

    fun opciones(): List<IconStyle> = ICONS
    fun fromKey(key: String?): IconStyle = ICONS.find { it.key == key } ?: DEFAULT
}
