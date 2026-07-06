package com.classdrop.ui.files

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivityFileDetailBinding
import com.classdrop.network.NetworkResult
import com.classdrop.utils.AlertUtils
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        archivoId = intent.getStringExtra("ARCHIVO_ID") ?: ""
        fileUrl = intent.getStringExtra("FILE_URL")
        fileName = intent.getStringExtra("FILE_NAME") ?: "Archivo"
        fileType = intent.getStringExtra("FILE_TYPE") ?: "PDF"

        // Contadores y estado real, tal como venían del Post en la lista de donde se abrió esta pantalla
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
        }

        // UI Initial states
        updateLikeUI()
        updateDislikeUI()
        updateBookmarkUI()
        updateDownloadUI()
        binding.tvDownloadsDetail.text = downloadsCount.toString()
    }

    private fun setupObservers() {
        commentsViewModel.commentsState.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> { }
                is NetworkResult.Success -> {
                    val listaReal = result.data ?: emptyList()
                    commentsAdapter.submitList(listaReal)
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        commentsViewModel.addCommentState.observe(this) { result ->
            if (result == null) return@observe
            when (result) {
                is NetworkResult.Loading -> {
                    binding.btnSendComment.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.btnSendComment.isEnabled = true
                    binding.etComment.text.clear()
                    commentsViewModel.fetchComments(archivoId)
                    binding.rvComments.scrollToPosition(0)
                }
                is NetworkResult.Error -> {
                    binding.btnSendComment.isEnabled = true
                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "Error al comentar",
                        message = result.message ?: "Inténtalo de nuevo",
                        type = AlertUtils.AlertType.ERROR
                    )
                    commentsViewModel.resetAddCommentState()
                }
            }
        }

        commentsViewModel.deleteCommentState.observe(this) { result ->
            if (result is NetworkResult.Success) {
                Toast.makeText(this, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                commentsViewModel.fetchComments(archivoId)
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFileData() {
        val fileSize = intent.getStringExtra("FILE_SIZE") ?: "0.0 MB"

        binding.tvFileNameLarge.text = fileName
        binding.tvFileTypeLarge.text = getString(R.string.file_type_size_format, fileType, fileSize)

        when (fileType.uppercase()) {
            "PDF" -> {
                binding.ivFileTypeIconLarge.setImageResource(R.drawable.ic_file_doc)
                binding.ivFileTypeIconLarge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.file_pdf_bg)
                binding.ivFileTypeIconLarge.setColorFilter(ContextCompat.getColor(this, R.color.file_pdf_text))
            }
            "PNG", "JPG", "JPEG" -> {
                binding.ivFileTypeIconLarge.setImageResource(R.drawable.ic_image)
                binding.ivFileTypeIconLarge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.file_pink_bg)
                binding.ivFileTypeIconLarge.setColorFilter(ContextCompat.getColor(this, R.color.file_pink_text))
            }
            else -> {
                binding.ivFileTypeIconLarge.setImageResource(R.drawable.ic_app_logo)
                binding.ivFileTypeIconLarge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.file_teal_bg)
                binding.ivFileTypeIconLarge.setColorFilter(ContextCompat.getColor(this, R.color.file_teal_text))
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCommentsList() {
        commentsAdapter = CommentsAdapter(
            onDeleteClick = { comentarioId -> commentsViewModel.deleteComment(comentarioId) },
            onLikeChanged = { comment -> commentsViewModel.actualizarLike(comment.id, comment.isLiked) },
            onDislikeChanged = { comment -> commentsViewModel.actualizarDislike(comment.id, comment.isDisliked) }
        )
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@FileDetailActivity)
            adapter = commentsAdapter
        }
    }

    private fun setupListeners() {
        // PREVISUALIZACIÓN: Al hacer clic en la tarjeta del archivo
        binding.root.findViewById<View>(R.id.ivFileTypeIconLarge).parent.let { card ->
            (card as View).setOnClickListener {
                if (!fileUrl.isNullOrEmpty()) {
                    val intent = Intent(this, FilePreviewActivity::class.java).apply {
                        putExtra("FILE_URL", fileUrl)
                        putExtra("FILE_NAME", fileName)
                        putExtra("FILE_TYPE", fileType)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "La previsualización no está disponible para este archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.llLikeDetail.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                likesCount++
                if (isDisliked) {
                    isDisliked = false
                    dislikesCount--
                    updateDislikeUI()
                    // Avisamos al backend que el dislike anterior debe quitarse,
                    // si no, el contador de dislikes queda desincronizado en el servidor.
                    if (archivoId.isNotEmpty()) filesViewModel.actualizarDislike(archivoId, false)
                }
            } else {
                likesCount--
            }
            updateLikeUI()
            animateButton(binding.ivLikeIconDetail)
            if (archivoId.isNotEmpty()) filesViewModel.actualizarLike(archivoId, isLiked)
        }

        binding.llDislikeDetail.setOnClickListener {
            isDisliked = !isDisliked
            if (isDisliked) {
                dislikesCount++
                if (isLiked) {
                    isLiked = false
                    likesCount--
                    updateLikeUI()
                    // Avisamos al backend que el like anterior debe quitarse,
                    // si no, el contador de likes queda desincronizado en el servidor.
                    if (archivoId.isNotEmpty()) filesViewModel.actualizarLike(archivoId, false)
                }
            } else {
                dislikesCount--
            }
            updateDislikeUI()
            animateButton(binding.ivDislikeIconDetail)
            if (archivoId.isNotEmpty()) filesViewModel.actualizarDislike(archivoId, isDisliked)
        }

        binding.llBookmarkDetail.setOnClickListener {
            isBookmarked = !isBookmarked
            updateBookmarkUI()
            animateButton(binding.ivBookmarkIconDetail)
            if (archivoId.isNotEmpty()) filesViewModel.actualizarFavorito(archivoId, isBookmarked)
        }

        binding.llDownloadDetail.setOnClickListener {
            AlertUtils.showCustomAlert(
                context = this,
                title = "Descargar archivo",
                message = "¿Deseas descargar este archivo en tu dispositivo?",
                type = AlertUtils.AlertType.CONFIRMATION,
                primaryButtonText = "Descargar",
                secondaryButtonText = "Cancelar",
                onPrimaryClick = {
                    isDownloaded = true
                    downloadsCount++
                    updateDownloadUI()
                    binding.tvDownloadsDetail.text = downloadsCount.toString()
                    animateButton(binding.ivDownloadIconDetail)

                    if (archivoId.isNotEmpty()) filesViewModel.registrarDescarga(archivoId)

                    if (!fileUrl.isNullOrEmpty()) {
                        startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(fileUrl)))
                    } else {
                        AlertUtils.showCustomAlert(
                            context = this,
                            title = "Sin archivo adjunto",
                            message = "Este archivo no tiene una URL disponible para abrir.",
                            type = AlertUtils.AlertType.ERROR
                        )
                    }
                }
            )
        }

        binding.btnSendComment.setOnClickListener {
            val content = binding.etComment.text.toString().trim()
            if (content.isNotBlank() && archivoId.isNotEmpty()) {
                commentsViewModel.postComment(archivoId, content)
            }
        }
    }

    private fun updateLikeUI() {
        val typedValue = android.util.TypedValue()
        val color = if (isLiked) {
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            typedValue.data
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true)
            typedValue.data
        }
        binding.ivLikeIconDetail.setColorFilter(color)
        binding.tvLikesDetail.apply {
            text = likesCount.toString()
            setTextColor(color)
        }
    }

    private fun updateDislikeUI() {
        val typedValue = android.util.TypedValue()
        val color = if (isDisliked) {
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            typedValue.data
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true)
            typedValue.data
        }
        binding.ivDislikeIconDetail.setColorFilter(color)
        binding.tvDislikesDetail.apply {
            text = dislikesCount.toString()
            setTextColor(color)
        }
    }

    private fun updateBookmarkUI() {
        val typedValue = android.util.TypedValue()
        val color = if (isBookmarked) {
            ContextCompat.getColor(this, android.R.color.holo_red_light)
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true)
            typedValue.data
        }
        binding.ivBookmarkIconDetail.setColorFilter(color)
        binding.tvBookmarkLabel.setTextColor(color)
    }

    private fun updateDownloadUI() {
        val typedValue = android.util.TypedValue()
        val color = if (isDownloaded) {
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            typedValue.data
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true)
            typedValue.data
        }
        binding.ivDownloadIconDetail.setColorFilter(color)
        binding.tvDownloadsDetail.setTextColor(color)
    }

    private fun animateButton(view: android.view.View) {
        view.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}