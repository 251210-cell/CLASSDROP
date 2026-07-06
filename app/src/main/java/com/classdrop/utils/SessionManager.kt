package com.classdrop.utils

import android.content.Context
import android.content.SharedPreferences
import com.classdrop.model.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("classdrop_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUserRole(role: UserRole) {
        prefs.edit().putString("user_role", role.name).apply()
    }

    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun fetchUserName(): String {
        return prefs.getString("user_name", "Usuario") ?: "Usuario"
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun fetchUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun fetchUserRole(): UserRole {
        val roleName = prefs.getString("user_role", UserRole.STUDENT.name)
        return try {
            UserRole.valueOf(roleName ?: UserRole.STUDENT.name)
        } catch (e: IllegalArgumentException) {
            UserRole.STUDENT
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun clearSessionData() {
        prefs.edit()
            .remove("auth_token")
            .remove("user_role")
            .apply()
    }

    // --- FAVORITOS SYNC ---
    fun toggleFavorite(postId: String) {
        val favorites = fetchFavorites().toMutableSet()
        if (favorites.contains(postId)) {
            favorites.remove(postId)
        } else {
            favorites.add(postId)
        }
        prefs.edit().putStringSet("favorite_ids", favorites).apply()
    }

    fun isFavorite(postId: String): Boolean {
        return fetchFavorites().contains(postId)
    }

    fun addFavorite(postId: String) {
        val favorites = fetchFavorites().toMutableSet()
        if (!favorites.contains(postId)) {
            favorites.add(postId)
            prefs.edit().putStringSet("favorite_ids", favorites).apply()
        }
    }

    fun removeFavorite(postId: String) {
        val favorites = fetchFavorites().toMutableSet()
        if (favorites.contains(postId)) {
            favorites.remove(postId)
            prefs.edit().putStringSet("favorite_ids", favorites).apply()
        }
    }

    fun fetchFavorites(): Set<String> {
        return prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
    }

    // --- LIKES / DISLIKES SYNC (archivos y comentarios) ---
    // El backend no nos devuelve si YO ya di like/dislike a un archivo o comentario
    // cuando se recarga la lista, así que lo recordamos localmente (igual que con
    // favoritos) para que el botón se quede resaltado sin importar cuántas veces
    // se recargue la pantalla o la lista.

    private fun fetchIds(key: String): Set<String> = prefs.getStringSet(key, emptySet()) ?: emptySet()

    private fun setActivo(key: String, id: String, activo: Boolean) {
        val ids = fetchIds(key).toMutableSet()
        if (activo) ids.add(id) else ids.remove(id)
        prefs.edit().putStringSet(key, ids).apply()
    }

    // Archivos
    fun isFileLiked(fileId: String): Boolean = fetchIds("liked_file_ids").contains(fileId)
    fun isFileDisliked(fileId: String): Boolean = fetchIds("disliked_file_ids").contains(fileId)

    fun setFileLiked(fileId: String, liked: Boolean) {
        setActivo("liked_file_ids", fileId, liked)
        if (liked) setActivo("disliked_file_ids", fileId, false)
    }

    fun setFileDisliked(fileId: String, disliked: Boolean) {
        setActivo("disliked_file_ids", fileId, disliked)
        if (disliked) setActivo("liked_file_ids", fileId, false)
    }

    // Comentarios
    fun isCommentLiked(commentId: String): Boolean = fetchIds("liked_comment_ids").contains(commentId)
    fun isCommentDisliked(commentId: String): Boolean = fetchIds("disliked_comment_ids").contains(commentId)

    fun setCommentLiked(commentId: String, liked: Boolean) {
        setActivo("liked_comment_ids", commentId, liked)
        if (liked) setActivo("disliked_comment_ids", commentId, false)
    }

    fun setCommentDisliked(commentId: String, disliked: Boolean) {
        setActivo("disliked_comment_ids", commentId, disliked)
        if (disliked) setActivo("liked_comment_ids", commentId, false)
    }
}