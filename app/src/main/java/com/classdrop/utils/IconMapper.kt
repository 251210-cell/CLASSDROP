package com.classdrop.utils

import com.classdrop.R

/**
 * Fuente única de verdad para los íconos de materia.
 *
 * El backend solo guarda una clave de texto en `MateriaResponse.icono` (ej. "code", "sigma").
 * Este objeto traduce esa clave al drawable + colores que se pintan en pantalla, y también
 * sirve para ir en la otra dirección cuando el admin elige un ícono en CreateSubjectActivity.
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
        IconStyle("sigma", R.drawable.ic_subject_sigma, "#EEF2FF", "#4F46E5"),
        IconStyle("database", R.drawable.ic_database, "#EFF6FF", "#2563EB"),
        IconStyle("structure", R.drawable.ic_subject_structure, "#FFF1F2", "#E11D48"),
        IconStyle("math", R.drawable.ic_subject_math, "#F0FDFA", "#0D9488"),
        IconStyle("calc", R.drawable.ic_subject_calc, "#FDF4FF", "#A21CAF")
    )

    private val DEFAULT = IconStyle("default", R.drawable.ic_mortarboard, "#EEF2FF", "#4F46E5")

    fun opciones(): List<IconStyle> = ICONS
    fun fromKey(key: String?): IconStyle = ICONS.find { it.key == key } ?: DEFAULT
}