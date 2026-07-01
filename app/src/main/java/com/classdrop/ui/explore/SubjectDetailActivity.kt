package com.classdrop.ui.explore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.R
import com.classdrop.databinding.ActivitySubjectDetailBinding
import com.classdrop.ui.main.MainActivity
import com.classdrop.utils.SessionManager



class SubjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubjectDetailBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Materia"
        val fileCount = intent.getIntExtra("FILE_COUNT", 0)

        binding.tvSubjectTitle.text = subjectName
        binding.tvSubtitle.text = "$fileCount archivos compartidos"

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpload.setOnClickListener {
            val intent = Intent(this, com.classdrop.ui.files.UploadFileActivity::class.java).apply {
                putExtra("SELECTED_SUBJECT", subjectName)
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

        // Al hacer clic en el avatar, ir al perfil (en MainActivity)
        binding.tvAvatarInitials.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("SELECT_TAB", "PROFILE")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private lateinit var filesViewModel: com.classdrop.viewmodel.FilesViewModel
    private lateinit var postsAdapter: PostsAdapter

    private fun setupPosts() {
        filesViewModel = androidx.lifecycle.ViewModelProvider(this)[com.classdrop.viewmodel.FilesViewModel::class.java]

        postsAdapter = PostsAdapter(
            sessionManager = sessionManager,
            onLikeChanged = { post -> filesViewModel.actualizarLike(post.id, post.isLiked) },
            onDislikeChanged = { post -> filesViewModel.actualizarDislike(post.id, post.isDisliked) }
        )
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postsAdapter

        val materiaId = intent.getStringExtra("SUBJECT_ID")

        filesViewModel.archivosPublicados.observe(this) { archivos ->
            val posts = archivos.map { mapFileToPost(it) }
            postsAdapter.submitList(posts)
            binding.tvSubtitle.text = "${posts.size} archivos compartidos"
        }

        filesViewModel.listError.observe(this) { error ->
            error?.let {
                com.classdrop.utils.AlertUtils.showCustomAlert(
                    context = this,
                    title = "No se pudo cargar",
                    message = it,
                    type = com.classdrop.utils.AlertUtils.AlertType.ERROR
                )
            }
        }

        filesViewModel.cargarArchivosPublicados(materiaId = materiaId)
    }

    private fun mapFileToPost(file: com.classdrop.model.FileModel): Post = Post(
        id = file.id,
        userName = file.autor?.nombreCompleto ?: "Usuario",
        time = "${com.classdrop.utils.TimeUtils.tiempoRelativo(file.creadoEn)} • ${file.materia?.nombre ?: ""}",
        fileName = file.titulo,
        fileType = file.tipo.uppercase(),
        likes = file.totalLikes,
        dislikes = file.totalDislikes,
        downloads = file.totalDescargas,
        comments = file.totalComentarios
    )
}