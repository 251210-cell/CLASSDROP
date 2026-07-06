package com.classdrop.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.ActivityAllFilesBinding
import com.classdrop.ui.explore.Post
import com.classdrop.ui.explore.PostsAdapter
import com.classdrop.ui.explore.toPost
import com.classdrop.utils.AlertUtils
import com.classdrop.utils.SessionManager
import com.classdrop.viewmodel.FilesViewModel

class AllFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllFilesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PostsAdapter
    private val filesViewModel: FilesViewModel by viewModels()

    // Las 3 secciones del perfil reutilizan esta misma pantalla, diferenciadas
    // por este extra (viene tal cual de ProfileFragment).
    private lateinit var fileType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        fileType = intent.getStringExtra("FILE_TYPE") ?: "Archivos"
        binding.tvTitle.text = fileType

        setupHeader()
        setupRecyclerView()
        setupObservers()
        cargarDatos()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupHeader() {
        val userName = sessionManager.fetchUserName()
        val initials = userName.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        binding.tvAvatarInitials.text = initials
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter(
            onBookmarkChanged = { post ->
                filesViewModel.actualizarFavorito(post.id, post.isBookmarked)
                // En la vista de Favoritos, si el usuario quita el corazón,
                // el archivo debe desaparecer de esta lista al instante.
                if (fileType == "Favoritos" && !post.isBookmarked) {
                    val listaActual = adapter.currentList.toMutableList()
                    listaActual.removeAll { it.id == post.id }
                    adapter.submitList(listaActual)
                    binding.tvEmptyState.visibility = if (listaActual.isEmpty()) View.VISIBLE else View.GONE
                }
            },
            onLikeChanged = { post -> filesViewModel.actualizarLike(post.id, post.isLiked) },
            onDislikeChanged = { post -> filesViewModel.actualizarDislike(post.id, post.isDisliked) },
            onDownloadConfirmed = { post -> descargarArchivo(post) }
        )

        binding.rvFiles.layoutManager = LinearLayoutManager(this)
        binding.rvFiles.adapter = adapter
        adapter.submitList(emptyList<Post>())
    }

    private fun setupObservers() {
        val liveData = when (fileType) {
            "Mis Archivos" -> filesViewModel.misArchivos
            "Descargas" -> filesViewModel.descargados
            "Favoritos" -> filesViewModel.favoritos
            else -> filesViewModel.misArchivos
        }

        liveData.observe(this) { archivos ->
            binding.pbAllFiles.visibility = View.GONE
            val posts = archivos.map { it.toPost() }
            adapter.submitList(posts)
            binding.rvFiles.visibility = if (posts.isEmpty()) View.GONE else View.VISIBLE
            binding.tvEmptyState.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        filesViewModel.listError.observe(this) { mensaje ->
            binding.pbAllFiles.visibility = View.GONE
            if (mensaje != null) {
                AlertUtils.showCustomAlert(
                    context = this,
                    title = "No se pudo cargar",
                    message = mensaje,
                    type = AlertUtils.AlertType.ERROR
                )
            }
        }
    }

    private fun cargarDatos() {
        binding.pbAllFiles.visibility = View.VISIBLE
        when (fileType) {
            "Mis Archivos" -> filesViewModel.cargarMisArchivos()
            "Descargas" -> filesViewModel.cargarDescargados()
            "Favoritos" -> filesViewModel.cargarFavoritos()
            else -> filesViewModel.cargarMisArchivos()
        }
    }

    /** Mismo comportamiento que en HomeFragment: registra la descarga como
     * estadística real en la API y abre el archivo (Firebase Storage / enlace). */
    private fun descargarArchivo(post: Post) {
        filesViewModel.registrarDescarga(post.id)
        if (!post.fileUrl.isNullOrEmpty()) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.fileUrl)))
        } else {
            AlertUtils.showCustomAlert(
                context = this,
                title = "No se pudo descargar",
                message = "Este archivo no tiene un enlace válido.",
                type = AlertUtils.AlertType.ERROR
            )
        }
    }
}