package com.classdrop.utils

import java.util.*
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {
    /**
     * Convierte bytes (Long) a un formato legible (B, KB, MB, GB, etc.)
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            Locale.US,
            "%.1f %s",
            size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    /**
     * Determina si un MIME type corresponde a una imagen
     */
    fun isImageMimeType(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/", ignoreCase = true) == true
    }
}
