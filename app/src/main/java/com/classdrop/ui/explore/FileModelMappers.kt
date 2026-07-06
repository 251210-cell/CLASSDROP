package com.classdrop.ui.explore

import com.classdrop.model.FileModel
import com.classdrop.utils.FileTypeUtils
import com.classdrop.utils.TimeUtils

/**
 * Convierte un FileModel (respuesta cruda del backend) en un Post (modelo de UI
 * que consume PostsAdapter). Se usa en AllFilesActivity y ProfileFragment.
 *
 * isLiked/isDisliked/isBookmarked se inicializan con la verdad real que ya manda
 * el backend (isLikedByMe/isDislikedByMe/isGuardadoByMe); PostsAdapter todavía los
 * puede sobrescribir con SessionManager si lo prefieres, pero partir de la verdad
 * del servidor es más confiable que empezar siempre en false.
 */
fun FileModel.toPost(): Post = Post(
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
    isLiked = isLikedByMe,
    isDisliked = isDislikedByMe,
    isBookmarked = isGuardadoByMe
)