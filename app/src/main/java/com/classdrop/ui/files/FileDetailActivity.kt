package com.classdrop.ui.files

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivityFileDetailBinding
import com.classdrop.model.Comment
import com.classdrop.network.NetworkResult
import com.classdrop.utils.AlertUtils
import com.classdrop.viewmodel.CommentsViewModel // CAMBIO 1: Importamos el ViewModel correcto de comentarios
import com.classdrop.viewmodel.FilesViewModel
import com.classdrop.viewmodel.AuthViewModel
import java.util.*

class FileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileDetailBinding
    private lateinit var commentsAdapter: CommentsAdapter
    private val filesViewModel: FilesViewModel by viewModels()
    // CAMBIO 2: Instanciamos el ViewModel de comentarios de tu API REST
    private val commentsViewModel: CommentsViewModel by viewModels()

    private var archivoId: String = "" // Guardará el ID dinámico del archivo
    private var isLiked = false
    private var isDisliked = false
    private var isBookmarked = false
    private var isDownloaded = false
    private var likesCount = 42
    private var dislikesCount = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CAMBIO 3: Recuperamos el ARCHIVO_ID dinámico enviado por tu Intent desde la lista de archivos
        archivoId = intent.getStringExtra("ARCHIVO_ID") ?: ""

        setupToolbar()
        setupCommentsList()
        setupListeners()
        setupObservers()
        loadFileData()

        // Cargar los comentarios reales desde la API al iniciar la pantalla
        if (archivoId.isNotEmpty()) {
            commentsViewModel.fetchComments(archivoId)
        }

        // Mock initial data
        updateLikeUI()
        updateDislikeUI()
        updateBookmarkUI()
        updateDownloadUI()
    }

    private fun setupObservers() {
        // CAMBIO 4: Observamos el estado de carga de la lista de comentarios de la API
        commentsViewModel.commentsState.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // Opcional: podrías mostrar una mini animación de carga
                }
                is NetworkResult.Success -> {
                    val listaReal = result.data ?: emptyList()
                    commentsAdapter.submitList(listaReal)
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // CAMBIO 5: Observamos el estado de publicación de un nuevo comentario
        commentsViewModel.addCommentState.observe(this) { result ->
            if (result == null) return@observe
            when (result) {
                is NetworkResult.Loading -> {
                    binding.btnSendComment.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.btnSendComment.isEnabled = true
                    binding.etComment.text.clear() // Limpiamos tu cuadro de texto original

                    // Recargamos los comentarios de la API para ver el nuevo e insertado
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

        // CAMBIO 6: Observamos el estado de eliminación de un comentario (clic largo)
        commentsViewModel.deleteCommentState.observe(this) { result ->
            if (result is NetworkResult.Success) {
                Toast.makeText(this, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                commentsViewModel.fetchComments(archivoId) // Refrescar lista
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFileData() {
        val fileName = intent.getStringExtra("FILE_NAME") ?: "Archivo sin nombre"
        val fileType = intent.getStringExtra("FILE_TYPE") ?: "PDF"
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
        // CAMBIO 7: Pasamos la lambda de borrado al constructor para solucionar el error de compilación
        commentsAdapter = CommentsAdapter { comentarioId ->
            // Clic largo en el comentario llama al borrado seguro de la API
            commentsViewModel.deleteComment(comentarioId)
        }
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@FileDetailActivity)
            adapter = commentsAdapter
        }
    }

    private fun setupListeners() {
        binding.llLikeDetail.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                likesCount++
                if (isDisliked) {
                    isDisliked = false
                    dislikesCount--
                    updateDislikeUI()
                }
            } else {
                likesCount--
            }
            updateLikeUI()
            animateButton(binding.ivLikeIconDetail)
        }

        binding.llDislikeDetail.setOnClickListener {
            isDisliked = !isDisliked
            if (isDisliked) {
                dislikesCount++
                if (isLiked) {
                    isLiked = false
                    likesCount--
                    updateLikeUI()
                }
            } else {
                dislikesCount--
            }
            updateDislikeUI()
            animateButton(binding.ivDislikeIconDetail)
        }

        binding.llBookmarkDetail.setOnClickListener {
            isBookmarked = !isBookmarked
            updateBookmarkUI()
            animateButton(binding.ivBookmarkIconDetail)
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
                    updateDownloadUI()
                    animateButton(binding.ivDownloadIconDetail)

                    AlertUtils.showCustomAlert(
                        context = this,
                        title = "¡Descarga Exitosa!",
                        message = "El archivo se ha descargado correctamente.",
                        type = AlertUtils.AlertType.SUCCESS
                    )
                }
            )
        }

        // CAMBIO 8: Vinculamos el botón de enviar al commentsViewModel real de tu API REST
        binding.btnSendComment.setOnClickListener {
            val content = binding.etComment.text.toString().trim()
            if (content.isNotBlank() && archivoId.isNotEmpty()) {
                // Dispara el flujo asíncrono hacia el backend
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