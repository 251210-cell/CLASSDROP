package com.classdrop.ui.files

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.classdrop.databinding.ActivityFilePreviewBinding
import com.classdrop.utils.AlertUtils
import java.net.URLEncoder

class FilePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilePreviewBinding

    private var fileUrl: String? = null
    private var fileName: String = "archivo"
    private var fileType: String = ""

    // Launcher para pedir el permiso de escritura en Android 9 (API 28) y anteriores.
    // Desde Android 10 (API 29) el DownloadManager puede escribir en la carpeta
    // pública de Descargas sin necesitar este permiso.
    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            iniciarDescarga()
        } else {
            Toast.makeText(
                this,
                "Se necesita permiso de almacenamiento para descargar el archivo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileName = intent.getStringExtra("FILE_NAME") ?: "Previsualización"
        fileUrl = intent.getStringExtra("FILE_URL")
        fileType = intent.getStringExtra("FILE_TYPE") ?: ""

        setupToolbar(fileName)

        val urlActual = fileUrl
        if (urlActual.isNullOrEmpty()) {
            binding.progressBar.visibility = View.GONE
            AlertUtils.showCustomAlert(
                this,
                "Error",
                "No se pudo encontrar la ubicación del archivo.",
                AlertUtils.AlertType.ERROR,
                onPrimaryClick = { finish() }
            )
            return
        }

        when {
            isImage(fileType) -> showImage(urlActual)
            fileType.equals("PDF", ignoreCase = true) -> showPdf(urlActual)
            else -> {
                binding.progressBar.visibility = View.GONE
                AlertUtils.showCustomAlert(
                    this,
                    "No soportado",
                    "La previsualización no está disponible para este tipo de archivo.",
                    AlertUtils.AlertType.WARNING,
                    onPrimaryClick = { finish() }
                )
            }
        }

        binding.btnDownloadPreview.setOnClickListener {
            AlertUtils.showCustomAlert(
                this,
                "Descargar",
                "¿Deseas descargar '$fileName'?",
                AlertUtils.AlertType.CONFIRMATION,
                primaryButtonText = "Descargar",
                onPrimaryClick = { verificarPermisoYDescargar() }
            )
        }
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.tvToolbarTitle.text = title
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun isImage(type: String): Boolean {
        val imageTypes = listOf("JPG", "JPEG", "PNG", "GIF", "BMP", "WEBP")
        return imageTypes.contains(type.uppercase())
    }

    private fun showImage(url: String) {
        binding.ivPreview.visibility = View.VISIBLE
        Glide.with(this)
            .load(url)
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(e: com.bumptech.glide.load.engine.GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>, isFirstResource: Boolean): Boolean {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@FilePreviewActivity, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                    return false
                }
                override fun onResourceReady(resource: android.graphics.drawable.Drawable, model: Any, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(binding.ivPreview)
    }

    private fun showPdf(url: String) {
        binding.wvPreview.visibility = View.VISIBLE
        binding.wvPreview.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
        }

        // IMPORTANTE: la URL del PDF (Firebase Storage) trae su propio "?alt=media&token=..."
        // Si se pega tal cual dentro de la query string de Google Docs Viewer, el "&token=..."
        // se interpreta como un parámetro nuevo de docs.google.com y rompe la URL real del PDF.
        // Por eso hay que codificarla (URL-encode) antes de concatenarla.
        val urlCodificada = URLEncoder.encode(url, "UTF-8")
        val googleDocsUrl = "https://docs.google.com/viewer?embedded=true&url=$urlCodificada"

        binding.wvPreview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                binding.progressBar.visibility = View.GONE
                binding.wvPreview.visibility = View.GONE
                AlertUtils.showCustomAlert(
                    this@FilePreviewActivity,
                    "No se pudo previsualizar",
                    "No fue posible cargar la vista previa del PDF. Puedes descargarlo para verlo.",
                    AlertUtils.AlertType.ERROR
                )
            }
        }
        binding.wvPreview.loadUrl(googleDocsUrl)
    }

    // ---------------------------------------------------------------------
    // DESCARGA REAL con DownloadManager (queda en la carpeta Descargas)
    // ---------------------------------------------------------------------

    private fun verificarPermisoYDescargar() {
        val necesitaPermiso = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P // API 28 o menor
        if (!necesitaPermiso) {
            iniciarDescarga()
            return
        }

        val yaConcedido = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (yaConcedido) {
            iniciarDescarga()
        } else {
            requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun iniciarDescarga() {
        val url = fileUrl
        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "No hay un archivo válido para descargar", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val nombreArchivo = construirNombreArchivo(url, fileName, fileType)

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(nombreArchivo)
                setDescription("Descargando desde ClassDrop")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombreArchivo)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, "Descargando '$nombreArchivo'...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            AlertUtils.showCustomAlert(
                this,
                "Error al descargar",
                e.message ?: "No se pudo iniciar la descarga del archivo.",
                AlertUtils.AlertType.ERROR
            )
        }
    }

    /**
     * Arma un nombre de archivo seguro para guardar en Descargas, con extensión real.
     * 1) Intenta sacar la extensión de la URL (ej. .../archivo.pdf?alt=media...)
     * 2) Si no la encuentra, usa el FILE_TYPE ("PDF", "JPG", etc.)
     */
    private fun construirNombreArchivo(url: String, nombreBase: String, tipo: String): String {
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
}