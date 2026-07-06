package com.classdrop.utils

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

/**
 * Lógica compartida para descargar un archivo remoto (Firebase Storage) con el
 * DownloadManager del sistema, usada tanto en FilePreviewActivity como en
 * FileDetailActivity para que el comportamiento sea idéntico en ambas pantallas.
 *
 * El registro del permiso en tiempo de ejecución (ActivityResultLauncher) se queda
 * en cada Activity porque Android exige que se registre antes de que la Activity
 * llegue a STARTED, pero toda la parte "pura" (armar nombre, armar el Request,
 * encolar la descarga) vive aquí para no duplicar código.
 */
object DownloadUtils {

    /** true si esta versión de Android requiere pedir WRITE_EXTERNAL_STORAGE en tiempo de ejecución. */
    fun necesitaPermisoDeEscritura(): Boolean =
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.P // API 28 (Android 9) o menor

    fun tienePermisoConcedido(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Arma un nombre de archivo seguro para guardar en Descargas, con extensión real.
     * 1) Intenta sacar la extensión de la URL (ej. .../archivo.pdf?alt=media...)
     * 2) Si no la encuentra, usa el tipo conocido ("PDF", "JPG", etc.)
     */
    fun construirNombreArchivo(url: String, nombreBase: String, tipo: String): String {
        val nombreLimpio = nombreBase
            .trim()
            .replace(Regex("[^a-zA-Z0-9 _\\-áéíóúÁÉÍÓÚñÑ]"), "_")
            .ifBlank { "archivo" }

        val extensionDesdeUrl = Uri.parse(url).lastPathSegment
            ?.let { Uri.decode(it) }
            ?.substringAfterLast('/', "")
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() && it.length in 2..5 }

        val extension = extensionDesdeUrl ?: when (tipo.uppercase()) {
            "PDF" -> "pdf"
            "JPG", "JPEG" -> "jpg"
            "PNG" -> "png"
            "GIF" -> "gif"
            "BMP" -> "bmp"
            "WEBP" -> "webp"
            "DOCX" -> "docx"
            "DOC" -> "doc"
            else -> ""
        }

        return if (extension.isNotBlank()) "$nombreLimpio.$extension" else nombreLimpio
    }

    /**
     * Encola la descarga real en el DownloadManager del sistema.
     * Devuelve el nombre final del archivo si todo salió bien, o lanza una excepción si algo falló
     * (por ejemplo, una URL inválida), para que quien llame decida cómo mostrar el error.
     */
    fun encolarDescarga(context: Context, url: String, nombreBase: String, tipo: String): String {
        val nombreArchivo = construirNombreArchivo(url, nombreBase, tipo)

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(nombreArchivo)
            setDescription("Descargando desde ClassDrop")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombreArchivo)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        return nombreArchivo
    }
}