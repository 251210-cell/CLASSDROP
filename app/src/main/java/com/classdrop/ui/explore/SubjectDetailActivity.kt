package com.classdrop.ui.explore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivitySubjectDetailBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager
import com.classdrop.utils.TimeUtils
import com.classdrop.viewmodel.FilesViewModel
import androidx.lifecycle.ViewModelProvider

class SubjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubjectDetailBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var filesViewModel: FilesViewModel
    private lateinit var postsAdapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Materia"
        val subjectId = intent.getStringExtra("SUBJECT_ID")

        binding.tvSubjectTitle.text = subjectName

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpload.setOnClickListener {
            val intent = Intent(this, com.classdrop.ui.files.UploadFileActivity::class.java).apply {
                putExtra("SELECTED_SUBJECT", subjectName)
                putExtra("SELECTED_SUBJECT_ID", subjectId)
            }
            startActivity(intent)
        }

        setupHeader()
        setupPosts()
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")

        binding.tvAvatarInitials.text = initials

        binding.tvAvatarInitials.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SELECT_TAB", "PROFILE")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupPosts() {
        filesViewModel = ViewModelProvider(this)[FilesViewModel::class.java]

        postsAdapter = PostsAdapter(
            sessionManager = sessionManager,
            onLikeChanged = { post -> filesViewModel.actualizarLike(post.id, post.isLiked) },
            onDislikeChanged = { post -> filesViewModel.actualizarDislike(post.id, post.isDisliked) },
            onBookmarkChanged = { post -> filesViewModel.actualizarFavorito(post.id, post.isBookmarked) },
            onDownloadConfirmed = { post -> descargarArchivo(post) }
        )
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postsAdapter

        val materiaId = intent.getStringExtra("SUBJECT_ID")

        filesViewModel.archivosPublicados.observe(this) { archivos ->
            binding.pbPosts.visibility = View.GONE
            val posts = archivos.map { mapFileToPost(it) }
            postsAdapter.submitList(posts)

            binding.tvSubtitle.text = "${posts.size} archivos compartidos"

            if (posts.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvPosts.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvPosts.visibility = View.VISIBLE
            }
        }

        filesViewModel.listError.observe(this) { error ->
            binding.pbPosts.visibility = View.GONE
            error?.let {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "No se pudo cargar",
                    message = it,
                    type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                )
            }
        }

        binding.pbPosts.visibility = View.VISIBLE
        binding.rvPosts.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE

        filesViewModel.cargarArchivosPublicados(materiaId = materiaId)
    }

    private fun mapFileToPost(file: com.classdrop.model.FileModel): Post = Post(
        id = file.id,
        userName = file.autor?.nombreCompleto ?: "Usuario",
        time = "${TimeUtils.tiempoRelativo(file.creadoEn)} • ${file.materia?.nombre ?: ""}",
        fileName = file.titulo,
        fileType = file.tipo.uppercase(),
        fileUrl = file.adjuntos?.firstOrNull()?.urlStorage,
        likes = file.totalLikes,
        dislikes = file.totalDislikes,
        downloads = file.totalDescargas,
        comments = file.totalComentarios
    )

    /** Registra la descarga en la API y abre el archivo real (Firebase Storage) en el navegador. */
    private fun descargarArchivo(post: Post) {
        filesViewModel.registrarDescarga(post.id)
        if (!post.fileUrl.isNullOrEmpty()) {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(post.fileUrl)))
        } else {
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = this,
                title = "No se pudo descargar",
                message = "Este archivo no tiene una URL disponible.",
                type = com.classdrop.utils.AlertUtils.AlertType.ERROR
            )
        }
    }
}