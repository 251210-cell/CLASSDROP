package com.classdrop.utils

import com.classdrop.model.Adjunto

/**
 * El campo `tipo` de FileModel es un ENUM de backend con solo 4 valores
 * ('pdf', 'docx', 'url', 'otro') y NO sirve para saber si un archivo es una
 * imagen (jpg, png, etc.), porque todo lo que no sea pdf/docx cae en "otro".
 *
 * Esta utilidad calcula el tipo REAL de archivo (para mostrar ícono y decidir
 * la previsualización) a partir del adjunto: primero mira la extensión del
 * nombre original, y si no puede, cae al mimetype. Si no hay nada de eso,
 * regresa el `tipo` del backend como último recurso.
 */
object FileTypeUtils {

    private val MIME_A_EXTENSION = mapOf(
        "application/pdf" to "PDF",
        "application/msword" to "DOCX",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "DOCX",
        "image/jpeg" to "JPG",
        "image/jpg" to "JPG",
        "image/png" to "PNG",
        "image/gif" to "GIF",
        "image/bmp" to "BMP",
        "image/webp" to "WEBP"
    )

    /**
     * @param adjunto El primer adjunto del archivo (puede ser null si es un enlace sin archivo).
     * @param tipoBackend El campo `tipo` original ("pdf", "docx", "url", "otro") usado como respaldo.
     */
    fun resolverTipoReal(adjunto: Adjunto?, tipoBackend: String): String {
        if (adjunto == null) return tipoBackend.uppercase()

        // 1) Intentar por extensión del nombre de archivo original
        val extension = adjunto.nombreOriginal
            .substringAfterLast('.', "")
            .uppercase()

        val extensionesConocidas = setOf(
            "PDF", "DOCX", "DOC", "JPG", "JPEG", "PNG", "GIF", "BMP", "WEBP"
        )
        if (extension.isNotBlank() && extension in extensionesConocidas) {
            return extension
        }

        // 2) Intentar por tipoMime del adjunto
        MIME_A_EXTENSION[adjunto.tipoMime]?.let { return it }

        // 3) Último recurso: el tipo genérico del backend
        return tipoBackend.uppercase()
    }
}