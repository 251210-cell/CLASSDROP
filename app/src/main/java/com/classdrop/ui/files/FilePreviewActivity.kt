package com.classdrop.ui.files

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.classdrop.databinding.ActivityFilePreviewBinding
import com.classdrop.utils.AlertUtils

class FilePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileName = intent.getStringExtra("FILE_NAME") ?: "Previsualización"
        val fileUrl = intent.getStringExtra("FILE_URL")
        val fileType = intent.getStringExtra("FILE_TYPE") ?: ""

        setupToolbar(fileName)

        if (fileUrl.isNullOrEmpty()) {
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
            isImage(fileType) -> showImage(fileUrl)
            fileType.equals("PDF", ignoreCase = true) -> showPdf(fileUrl)
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
            // Lógica de descarga (simulada)
            AlertUtils.showCustomAlert(
                this,
                "Descargar",
                "¿Deseas descargar '$fileName'?",
                AlertUtils.AlertType.CONFIRMATION,
                primaryButtonText = "Descargar",
                onPrimaryClick = {
                    Toast.makeText(this, "Descargando archivo...", Toast.LENGTH_SHORT).show()
                }
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
        
        // Usando el visor de Google Docs para previsualizar el PDF remoto
        val googleDocsUrl = "https://docs.google.com/viewer?embedded=true&url=$url"
        
        binding.wvPreview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@FilePreviewActivity, "Error al cargar PDF", Toast.LENGTH_SHORT).show()
            }
        }
        binding.wvPreview.loadUrl(googleDocsUrl)
    }
}
