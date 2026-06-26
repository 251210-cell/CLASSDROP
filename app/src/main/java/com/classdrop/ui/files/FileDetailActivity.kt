package com.classdrop.ui.files

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.classdrop.viewmodel.FilesViewModel
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivityFileDetailBinding
import com.classdrop.model.Comment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class FileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileDetailBinding
    private lateinit var commentsAdapter: CommentsAdapter
    private val viewModel: FilesViewModel by viewModels()
    
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

        setupToolbar()
        setupCommentsList()
        setupListeners()
        setupObservers()
        loadFileData()
        
        // Mock initial data
        updateLikeUI()
        updateDislikeUI()
        updateBookmarkUI()
        updateDownloadUI()
    }

    private fun setupObservers() {
        viewModel.comments.observe(this) { comments ->
            commentsAdapter.submitList(comments)
        }
    }

    private fun loadFileData() {
        val fileName = intent.getStringExtra("FILE_NAME") ?: "Archivo sin nombre"
        val fileType = intent.getStringExtra("FILE_TYPE") ?: "PDF"
        val fileSize = intent.getStringExtra("FILE_SIZE") ?: "0.0 MB"

        binding.tvFileNameLarge.text = fileName
        binding.tvFileTypeLarge.text = getString(R.string.file_type_size_format, fileType, fileSize)
        
        // Dynamic colors and icons based on type
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
        commentsAdapter = CommentsAdapter()
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
            MaterialAlertDialogBuilder(this)
                .setTitle("Descargar archivo")
                .setMessage("¿Deseas descargar este archivo en tu dispositivo?")
                .setPositiveButton("Descargar") { _, _ ->
                    isDownloaded = true
                    updateDownloadUI()
                    animateButton(binding.ivDownloadIconDetail)
                    Toast.makeText(this, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.btnSendComment.setOnClickListener {
            val content = binding.etComment.text.toString()
            if (content.isNotBlank()) {
                viewModel.addComment(content)
                binding.etComment.text.clear()
                binding.rvComments.scrollToPosition(0)
            }
        }
    }

    private fun updateLikeUI() {
        val colorActive = Color.parseColor("#A855F7")
        val colorInactive = ContextCompat.getColor(this, R.color.placeholder)
        val color = if (isLiked) colorActive else colorInactive
        binding.ivLikeIconDetail.setColorFilter(color)
        binding.tvLikesDetail.apply {
            text = likesCount.toString()
            setTextColor(color)
        }
    }

    private fun updateDislikeUI() {
        val colorActive = Color.parseColor("#A855F7")
        val colorInactive = ContextCompat.getColor(this, R.color.placeholder)
        val color = if (isDisliked) colorActive else colorInactive
        binding.ivDislikeIconDetail.setColorFilter(color)
        binding.tvDislikesDetail.apply {
            text = dislikesCount.toString()
            setTextColor(color)
        }
    }

    private fun updateBookmarkUI() {
        val colorActive = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val colorInactive = ContextCompat.getColor(this, R.color.placeholder)
        val color = if (isBookmarked) colorActive else colorInactive
        binding.ivBookmarkIconDetail.setColorFilter(color)
        binding.tvBookmarkLabel.setTextColor(color)
    }

    private fun updateDownloadUI() {
        // As requested: same color as other active views (Purple #A855F7)
        val colorActive = Color.parseColor("#A855F7")
        val colorInactive = ContextCompat.getColor(this, R.color.placeholder)
        val color = if (isDownloaded) colorActive else colorInactive
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
