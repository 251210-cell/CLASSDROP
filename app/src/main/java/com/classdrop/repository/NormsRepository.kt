package com.classdrop.repository

import android.content.Context
import android.content.SharedPreferences
import com.classdrop.data.local.AppDatabase
import com.classdrop.data.local.dao.PrivacyRuleDao
import com.classdrop.data.mapper.toDomain
import com.classdrop.data.mapper.toEntity
import com.classdrop.model.CommunityRule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NormsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("community_norms_prefs", Context.MODE_PRIVATE)
    private val privacyRuleDao: PrivacyRuleDao = AppDatabase.getDatabase(context).privacyRuleDao()
    private val gson = Gson()

    fun saveRules(rules: List<CommunityRule>) {
        val json = gson.toJson(rules)
        prefs.edit().putString("saved_rules", json).apply()
    }

    fun getRules(): List<CommunityRule> {
        val json = prefs.getString("saved_rules", null) ?: return emptyList()
        val type = object : TypeToken<List<CommunityRule>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun saveSanctions(description: String) {
        prefs.edit().putString("sanctions_desc", description).apply()
    }
    
    fun getSanctions(): String {
        return prefs.getString("sanctions_desc", "El incumplimiento de estas normas podrá conllevar la aplicación de medidas correctivas progresivas.") ?: ""
    }

    fun savePrivacyPolicy(content: String) {
        prefs.edit().putString("privacy_policy", content).apply()
    }

    fun getPrivacyPolicy(): String {
        return prefs.getString("privacy_policy", "En ClassDrop nos tomamos en serio la privacidad de nuestros usuarios universitarios. Los datos recolectados (nombre, correo institucional y archivos subidos) se utilizan exclusivamente para fines académicos y de mejora de la experiencia educativa.\n\nNo compartimos información con terceros sin consentimiento explícito.") ?: ""
    }

    // Room persistence for Privacy Rules
    fun getPrivacyRulesFlow(): Flow<List<CommunityRule>> {
        return privacyRuleDao.getAllRules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun savePrivacyRules(rules: List<CommunityRule>) {
        privacyRuleDao.insertRules(rules.map { it.toEntity() })
    }

    suspend fun savePrivacyRule(rule: CommunityRule) {
        privacyRuleDao.insertRule(rule.toEntity())
    }

    suspend fun deletePrivacyRule(rule: CommunityRule) {
        privacyRuleDao.deleteRule(rule.toEntity())
    }

    fun getPrivacyRules(): List<CommunityRule> {
        // Fallback or legacy support if needed, but we should use Flow for Room
        val json = prefs.getString("saved_privacy_rules", null) ?: return emptyList()
        val type = object : TypeToken<List<CommunityRule>>() {}.type
        return gson.fromJson(json, type)
    }

    fun savePrivacyHeader(description: String) {
        prefs.edit().putString("privacy_header_desc", description).apply()
    }

    fun getPrivacyHeader(): String {
        return prefs.getString("privacy_header_desc", "En ClassDrop, nos tomamos en serio tu privacidad. Nos comprometemos a proteger tus datos personales asegurando un entorno colaborativo transparente y seguro para la comunidad universitaria.") ?: ""
    }
}
