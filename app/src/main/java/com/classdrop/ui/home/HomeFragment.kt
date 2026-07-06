package com.classdrop.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.classdrop.databinding.FragmentHomeBinding
import com.classdrop.model.FileModel
import com.classdrop.model.MateriaResponse
import com.classdrop.model.UserRole
import com.classdrop.ui.explore.Post
import com.classdrop.ui.explore.PostsAdapter
import com.classdrop.ui.explore.SubjectDetailActivity
import com.classdrop.ui.main.MainActivity
import com.classdrop.ui.notifications.NotificationsActivity
import com.classdrop.utils.FileTypeUtils
import com.classdrop.utils.SessionManager
import com.classdrop.utils.TimeUtils
import com.classdrop.viewmodel.FilesViewModel
import com.classdrop.viewmodel.SubjectsViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: SubjectsViewModel by viewModels()
    private val filesViewModel: FilesViewModel by viewModels()
    private lateinit var adapter: SubjectsAdapter
    private lateinit var postsAdapter: PostsAdapter
    private var skipNextResumeRefresh = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        skipNextResumeRefresh = true
        setupUI()
        setupListeners()
        setupNovedades()

        refreshData(showLoading = true)
    }

    override fun onResume() {
        super.onResume()
        if (skipNextResumeRefresh) {
            skipNextResumeRefresh = false
            return
        }
        // Vuelve desde otra Activity (p. ej. notificaciones): refresco silencioso.
        refreshData(showLoading = false)
    }

    private fun refreshData(showLoading: Boolean) {
        val materiasCache = viewModel.materias.value
        if (materiasCache != null) {
            renderMaterias(materiasCache)
        } else if (showLoading) {
            binding.pbSubjects.visibility = View.VISIBLE
            binding.rvSubjects.visibility = View.GONE
        }
        viewModel.fetchAllMaterias()

        if (sessionManager.fetchUserRole() != UserRole.ADMIN) {
            val archivosCache = filesViewModel.archivosPublicados.value
            if (archivosCache != null) {
                renderNovedades(archivosCache)
            } else if (showLoading) {
                binding.pbNovedades.visibility = View.VISIBLE
                binding.rvNovedades.visibility = View.GONE
                binding.tvNovedadesVacio.visibility = View.GONE
            }
            filesViewModel.cargarArchivosPublicados()
        }
    }

    private fun renderMaterias(materias: List<MateriaResponse>) {
        binding.pbSubjects.visibility = View.GONE
        binding.rvSubjects.visibility = View.VISIBLE
        adapter.submitList(materias)
    }

    private fun renderNovedades(archivos: List<FileModel>) {
        binding.pbNovedades.visibility = View.GONE
        val posts = archivos.map { it.toPost() }
        postsAdapter.submitList(posts)

        if (posts.isEmpty()) {
            binding.tvNovedadesVacio.visibility = View.VISIBLE
            binding.rvNovedades.visibility = View.GONE
        } else {
            binding.tvNovedadesVacio.visibility = View.GONE
            binding.rvNovedades.visibility = View.VISIBLE
        }
    }

    private fun setupUI() {
        val userName = sessionManager.fetchUserName()
        val userRole = sessionManager.fetchUserRole()

        if (userRole == UserRole.ADMIN) {
            binding.adminBannerCard.visibility = View.VISIBLE
            binding.adminToolsLayout.visibility = View.VISIBLE
            binding.ivNotification.visibility = View.GONE
            binding.viewNotificationDot.visibility = View.GONE

            binding.saludoLayout.visibility = View.GONE
            binding.novedadesHeader.visibility = View.GONE
            binding.flNovedades.visibility = View.GONE
        } else {
            binding.adminBannerCard.visibility = View.GONE
            binding.adminToolsLayout.visibility = View.GONE
            binding.ivNotification.visibility = View.VISIBLE

            binding.saludoLayout.visibility = View.VISIBLE
            binding.novedadesHeader.visibility = View.VISIBLE
            binding.flNovedades.visibility = View.VISIBLE

            binding.tvSaludo.text = "¡Hola, $userName!"
        }

        val initials = if (userName.length >= 2) {
            userName.split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")
        } else if (userName.isNotEmpty()) {
            userName.take(1).uppercase()
        } else {
            "?"
        }
        binding.tvAvatarInitials.text = initials
    }

    private fun setupListeners() {
        binding.tvAvatarInitials.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.Tab.PROFILE)
        }

        binding.ivNotification.setOnClickListener {
            binding.viewNotificationDot.visibility = View.GONE
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        binding.tvSeeAllSubjects.setOnClickListener {
            val userRole = sessionManager.fetchUserRole()
            val activityClass = if (userRole == UserRole.ADMIN) {
                com.classdrop.ui.admin.SubjectsAdminActivity::class.java
            } else {
                AllSubjectsActivity::class.java
            }
            startActivity(Intent(requireContext(), activityClass))
        }

        adapter = SubjectsAdapter { materia ->
            navigateToSubject(materia)
        }
        binding.rvSubjects.adapter = adapter

        viewModel.materias.observe(viewLifecycleOwner) { listaMaterias ->
            renderMaterias(listaMaterias)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            binding.pbSubjects.visibility = View.GONE
        }
    }

    private fun setupNovedades() {
        postsAdapter = PostsAdapter(
            onLikeChanged = { post -> filesViewModel.actualizarLike(post.id, post.isLiked) },
            onDislikeChanged = { post -> filesViewModel.actualizarDislike(post.id, post.isDisliked) },
            onBookmarkChanged = { post -> filesViewModel.actualizarFavorito(post.id, post.isBookmarked) },
            onDownloadConfirmed = { post -> descargarArchivo(post) }
        )
        binding.rvNovedades.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNovedades.adapter = postsAdapter

        filesViewModel.archivosPublicados.observe(viewLifecycleOwner) { archivos ->
            renderNovedades(archivos)
        }

        filesViewModel.listError.observe(viewLifecycleOwner) {
            binding.pbNovedades.visibility = View.GONE
        }
    }

    private fun FileModel.toPost(): Post = Post(
        id = id,
        userName = autor?.nombreCompleto ?: "Usuario",
        time = "${TimeUtils.tiempoRelativo(creadoEn)} • ${materia?.nombre ?: ""}",
        fileName = titulo,
        fileType = FileTypeUtils.resolverTipoReal(adjuntos?.firstOrNull(), tipo),
        fileUrl = adjuntos?.firstOrNull()?.urlStorage,
        likes = totalLikes,
        dislikes = totalDislikes,
        downloads = totalDescargas,
        comments = totalComentarios,
        // Verdad real del backend (calculada por usuario en cada consulta), igual que
        // ya se hace con los comentarios. Ya no dependemos de SessionManager para esto.
        isLiked = isLikedByMe,
        isDisliked = isDislikedByMe,
        isBookmarked = isGuardadoByMe
    )

    private fun navigateToSubject(subject: MateriaResponse) {
        val intent = Intent(requireContext(), SubjectDetailActivity::class.java).apply {
            putExtra("SUBJECT_ID", subject.id)
            putExtra("SUBJECT_NAME", subject.nombre)
        }
        startActivity(intent)
    }

    /** Registra la descarga en la API y abre el archivo real (Firebase Storage) en el navegador. */
    private fun descargarArchivo(post: Post) {
        filesViewModel.registrarDescarga(post.id)
        if (!post.fileUrl.isNullOrEmpty()) {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(post.fileUrl)))
        } else {
            com.classdrop.utils.AlertUtils.showCustomAlert(
                context = requireContext(),
                title = "No se pudo descargar",
                message = "Este archivo no tiene una URL disponible.",
                type = com.classdrop.utils.AlertUtils.AlertType.ERROR
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}