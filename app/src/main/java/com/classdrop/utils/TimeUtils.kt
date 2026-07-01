package com.classdrop.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object TimeUtils {

    /**
     * Convierte una fecha ISO-8601 (ej. "2026-06-30T18:22:00.000Z", tal como la
     * serializa Sequelize/Postgres) a un texto relativo tipo "hace 30 min".
     */
    fun tiempoRelativo(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val formatos = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
            )
            var fechaMillis: Long? = null
            for (patron in formatos) {
                try {
                    val sdf = SimpleDateFormat(patron, Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    fechaMillis = sdf.parse(isoDate)?.time
                    if (fechaMillis != null) break
                } catch (_: Exception) { /* intenta el siguiente patrón */ }
            }
            if (fechaMillis == null) return ""

            val diffMs = System.currentTimeMillis() - fechaMillis
            val minutos = diffMs / 60000
            val horas = minutos / 60
            val dias = horas / 24

            when {
                minutos < 1 -> "justo ahora"
                minutos < 60 -> "hace $minutos min"
                horas < 24 -> "hace $horas h"
                dias < 2 -> "ayer"
                else -> "hace $dias días"
            }
        } catch (e: Exception) {
            ""
        }
    }
}