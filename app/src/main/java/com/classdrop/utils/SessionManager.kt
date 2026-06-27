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
}