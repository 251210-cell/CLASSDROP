package com.classdrop.ui.files

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.classdrop.R
import com.classdrop.databinding.ActivityFileDetailBinding
import com.classdrop.network.NetworkResult
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.DownloadUtils
import com.classdrop.viewmodel.CommentsViewModel
import com.classdrop.viewmodel.FilesViewModel

class FileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileDetailBinding
    private lateinit var commentsAdapter: CommentsAdapter
    private val filesViewModel: FilesViewModel by viewModels()
    private val commentsViewModel: CommentsViewModel by viewModels()

    private var archivoId: String = ""
    private var fileUrl: String? = null
    private var fileName: String = ""
    private var fileType: String = ""

    private var isLiked = false
    private var isDisliked = false
    private var isBookmarked = false
    private var isDownloaded = false
    private var likesCount = 0
    private var dislikesCount = 0
    private var downloadsCount = 0

    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) descargarArchivoReal()
        else Toast.makeText(this, "Se necesita permiso para descargar el archivo", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        archivoId = intent.getStringExtra("ARCHIVO_ID") ?: ""
        fileUrl = intent.getStringExtra("FILE_URL")
        fileName = intent.getStringExtra("FILE_NAME") ?: "Archivo"
        fileType = intent.getStringExtra("FILE_TYPE") ?: "PDF"

        likesCount = intent.getIntExtra("FILE_LIKES", 0)
        dislikesCount = intent.getIntExtra("FILE_DISLIKES", 0)
        downloadsCount = intent.getIntExtra("FILE_DOWNLOADS", 0)
        isLiked = intent.getBooleanExtra("FILE_IS_LIKED", false)
        isDisliked = intent.getBooleanExtra("FILE_IS_DISLIKED", false)
        isBookmarked = intent.getBooleanExtra("FILE_IS_BOOKMARKED", false)

        setupToolbar()
        setupCommentsList()
        setupListeners()
        setupObservers()
        loadFileData()

        if (archivoId.isNotEmpty()) {
            commentsViewModel.fetchComments(archivoId)
            filesViewModel.cargarArchivo(archivoId)
        }

        updateLikeUI()
        updateDislikeUI()
        updateBookmarkUI()
        updateDownloadUI()
        binding.tvDownloadsDetail.text = downloadsCount.toString()
    }

    private fun loadFileData() {
        val fileSize = intent.getStringExtra("FILE_SIZE") ?: "0.0 MB"
        binding.tvFileNameLarge.text = fileName
        binding.tvFileTypeLarge.text = getString(R.string.file_type_size_format, fileType.uppercase(), fileSize)
        
        binding.tvFileTypeBadge.text = fileType.uppercase()

        val fileTypeUpper = fileType.uppercase()
        val isImage = fileTypeUpper in listOf("PNG", "JPG", "JPEG", "IMG", "IMAGE") || 
                      (fileUrl?.lowercase()?.let { it.contains(".jpg") || it.contains(".png") || it.contains(".jpeg") } ?: false)
        
        if (isImage && !fileUrl.isNullOrEmpty()) {
            binding.ivFileTypeIconLarge.apply {
                // LIMPIEZA TOTAL: Forzamos que no haya fondos ni tintes previos
                setPadding(0, 0, 0, 0)
                setBackgroundResource(0) 
                background = null
                backgroundTintList = null
                imageTintList = null
                setColorFilter(null)
                scaleType = ImageView.ScaleType.CENTER_CROP
                
                Glide.with(this@FileDetailActivity)
                    .load(fileUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(this)
            }
            binding.tvFileTypeBadge.setTextColor(ContextCompat.getColor(this, R.color.file_teal_text))
            binding.tvFileTypeBadge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.file_teal_bg)
        } else {
            when (fileTypeUpper) {
                "PDF" -> setupIconUI(R.drawable.ic_mortarboard, R.color.file_pdf_bg, R.color.file_pdf_text)
                "DOCX", "DOC" -> setupIconUI(R.drawable.ic_file_doc, R.color.file_pink_bg, R.color.file_pink_text)
                else -> setupIconUI(R.drawable.ic_file_doc, R.color.surface_variant, R.color.text_secondary)
            }
        }
    }

    private fun setupIconUI(iconRes: Int, bgColorRes: Int, textColorRes: Int) {
        binding.ivFileTypeIconLarge.apply {
            val p = (20 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, p)
            setImageResource(iconRes)
            background = ContextCompat.getDrawable(this@FileDetailActivity, R.drawable.bg_rounded_square_primary)
            backgroundTintList = ContextCompat.getColorStateList(this@FileDetailActivity, bgColorRes)
            setColorFilter(ContextCompat.getColor(this@FileDetailActivity, textColorRes))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        binding.tvFileTypeBadge.setTextColor(ContextCompat.getColor(this, textColorRes))
        binding.tvFileTypeBadge.backgroundTintList = ContextCompat.getColorStateList(this, bgColorRes)
    }

    private fun setupObservers() {
        filesViewModel.archivoDetalle.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val archivo = result.data ?: return@observe
                likesCount = archivo.totalLikes
                dislikesCount = archivo.totalDislikes
                downloadsCount = archivo.totalDescargas
                isLiked = archivo.isLikedByMe
                isDisliked = archivo.isDislikedByMe
                isBookmarked = archivo.isGuardadoByMe
                
                archivo.adjuntos?.firstOrNull()?.let { adjunto ->
                    fileUrl = adjunto.urlStorage ?: fileUrl
                    fileType = com.classdrop.utils.FileTypeUtils.resolverTipoReal(adjunto, archivo.tipo)
                }
                
                fileName = archivo.titulo
                loadFileData()
                updateLikeUI()
                updateDislikeUI()
                updateBookmarkUI()
                binding.tvDownloadsDetail.text = downloadsCount.toString()
            }
        }

        commentsViewModel.commentsState.observe(this) { result ->
            if (result is NetworkResult.Success) commentsAdapter.submitList(result.data ?: emptyList())
        }

        commentsViewModel.addCommentState.observe(this) { result ->
            if (result is NetworkResult.Success) {
                binding.btnSendComment.isEnabled = true
                binding.etComment.text.clear()
                commentsViewModel.fetchComments(archivoId)
                binding.rvComments.scrollToPosition(0)
            } else if (result is NetworkResult.Loading) binding.btnSendComment.isEnabled = false
        }
    }

    private fun setupListeners() {
        // PREVISUALIZACIÓN: Al tocar la imagen o el nombre se abre el visor interno
        binding.ivFileTypeIconLarge.setOnClickListener { openFilePreview() }
        binding.tvFileNameLarge.setOnClickListener { openFilePreview() }

        binding.llLikeDetail.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                likesCount++
                if (isDisliked) { isDisliked = false; dislikesCount--; updateDislikeUI() }
            } else { likesCount-- }
            updateLikeUI(); animateButton(binding.ivLikeIconDetail)
            if (archivoId.isNotEmpty()) filesViewModel.actualizarLike(archivoId, isLiked)
        }

        binding.llDislikeDetail.setOnClickListener {
            isDisliked = !isDisliked
            if (isDisliked) {
                dislikesCount++
                if (isLiked) { isLiked = false; likesCount--; updateLikeUI() }
            } else { dislikesCount-- }
            updateDislikeUI(); animateButton(binding.ivDislikeIconDetail)
            if (archivoId.isNotEmpty()) filesViewModel.actualizarDislike(archivoId, isDisliked)
        }

        binding.llBookmarkDetail.setOnClickListener {
            isBookmarked = !isBookmarked
            updateBookmarkUI(); animateButton(binding.ivBookmarkIconDetail)
            if (archivoId.isNotEmpty()) filesViewModel.actualizarFavorito(archivoId, isBookmarked)
        }

        binding.llDownloadDetail.setOnClickListener {
            AlertUtils.showCustomAlert(this, "Descargar", "¿Deseas descargar este archivo?", AlertUtils.AlertType.CONFIRMATION, primaryButtonText = "Descargar", secondaryButtonText = "Cancelar",
                onPrimaryClick = {
                    isDownloaded = true; downloadsCount++; updateDownloadUI()
                    binding.tvDownloadsDetail.text = downloadsCount.toString()
                    animateButton(binding.ivDownloadIconDetail)
                    if (archivoId.isNotEmpty()) filesViewModel.registrarDescarga(archivoId)
                    verificarPermisoYDescargar()
                }
            )
        }

        binding.btnSendComment.setOnClickListener {
            val content = binding.etComment.text.toString().trim()
            if (content.isNotBlank() && archivoId.isNotEmpty()) commentsViewModel.postComment(archivoId, content)
        }
    }

    private fun openFilePreview() {
        if (!fileUrl.isNullOrEmpty()) {
            val intent = Intent(this, FilePreviewActivity::class.java).apply {
                putExtra("FILE_NAME", fileName)
                putExtra("FILE_URL", fileUrl)
                putExtra("FILE_TYPE", fileType)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "La URL del archivo no está disponible todavía", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarPermisoYDescargar() {
        if (!DownloadUtils.necesitaPermisoDeEscritura() || DownloadUtils.tienePermisoConcedido(this)) descargarArchivoReal()
        else requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun descargarArchivoReal() {
        fileUrl?.let { url ->
            val nombreArchivo = DownloadUtils.encolarDescarga(this, url, fileName, fileType)
            Toast.makeText(this, "Descargando '$nombreArchivo'...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar); supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCommentsList() {
        commentsAdapter = CommentsAdapter(
            onDeleteClick = { id -> commentsViewModel.deleteComment(id) },
            onLikeChanged = { c -> commentsViewModel.actualizarLike(archivoId, c.id, c.isLiked) },
            onDislikeChanged = { c -> commentsViewModel.actualizarDislike(archivoId, c.id, c.isDisliked) }
        )
        binding.rvComments.apply { layoutManager = LinearLayoutManager(this@FileDetailActivity); adapter = commentsAdapter }
    }

    private fun updateLikeUI() {
        val color = if (isLiked) ContextCompat.getColor(this, R.color.primary) else ContextCompat.getColor(this, R.color.outline)
        binding.ivLikeIconDetail.setColorFilter(color); binding.tvLikesDetail.apply { text = likesCount.toString(); setTextColor(color) }
    }

    private fun updateDislikeUI() {
        val color = if (isDisliked) ContextCompat.getColor(this, R.color.primary) else ContextCompat.getColor(this, R.color.outline)
        binding.ivDislikeIconDetail.setColorFilter(color); binding.tvDislikesDetail.apply { text = dislikesCount.toString(); setTextColor(color) }
    }

    private fun updateBookmarkUI() {
        val color = if (isBookmarked) ContextCompat.getColor(this, android.R.color.holo_red_light) else ContextCompat.getColor(this, R.color.outline)
        binding.ivBookmarkIconDetail.setColorFilter(color); binding.tvBookmarkLabel.setTextColor(color)
    }

    private fun updateDownloadUI() {
        val color = if (isDownloaded) ContextCompat.getColor(this, R.color.primary) else ContextCompat.getColor(this, R.color.outline)
        binding.ivDownloadIconDetail.setColorFilter(color); binding.tvDownloadsDetail.setTextColor(color)
    }

    private fun animateButton(view: View) {
        view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction { view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start() }.start()
    }
}
