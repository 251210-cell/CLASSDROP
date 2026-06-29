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
        val json = prefs.getString("saved_rules", null)
        if (json == null) {
            return getDefaultCommunityRules()
        }
        val type = object : TypeToken<List<CommunityRule>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun getDefaultCommunityRules(): List<CommunityRule> {
        return listOf(
            CommunityRule(
                id = "1",
                title = "Propósito y Alcance",
                description = "ClassDrop es una plataforma diseñada para el intercambio de conocimiento. Este reglamento es vinculante para todo usuario registrado y se aplica a todas las interacciones dentro del ecosistema digital de la plataforma."
            ),
            CommunityRule(
                id = "2",
                title = "Uso de la Plataforma",
                description = "Los usuarios son responsables por el uso y seguridad de sus credenciales de acceso. Se prohíbe la creación de cuentas múltiples para evadir sanciones o manipular sistemas de reputación."
            ),
            CommunityRule(
                id = "3",
                title = "Integridad Académica",
                description = "Queda estrictamente prohibido el intercambio de material que fomente el fraude académico, incluyendo pero no limitado a: exámenes vigentes, respuestas de evaluaciones o cualquier método de suplantación."
            ),
            CommunityRule(
                id = "4",
                title = "Propiedad Intelectual",
                description = "Al subir contenido, el usuario garantiza poseer los derechos necesarios o contar con autorización. ClassDrop respetará las leyes de Copyright, el material que infrinja derechos de autor será removido tras una notificación válida."
            ),
            CommunityRule(
                id = "5",
                title = "Comportamiento Social",
                description = "Se exige un trato respetuoso: no se tolera el acoso, la discriminación por cualquier motivo, ni el lenguaje de odio. Las discusiones deben mantenerse en un marco de crítica constructiva."
            ),
            CommunityRule(
                id = "6",
                title = "Moderación y Apelaciones",
                description = "El equipo de moderación tiene la facultad de retirar contenido y suspender cuentas. Los usuarios afectados tienen derecho a una sola apelación formal a través del Centro de Soporte dentro de las 72hs posteriores a la sanción."
            )
        )
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
            if (entities.isEmpty()) {
                getDefaultPrivacyRules()
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    fun getDefaultPrivacyRules(): List<CommunityRule> {
        return listOf(
            CommunityRule(
                id = "p1",
                title = "Recopilación de Datos",
                description = "Únicamente recabamos tu nombre completo y tu correo electrónico institucional de la UPChiapas al registrarte. Te garantizamos que ClassDrop no solicita ni trata ningún tipo de datos personales sensibles."
            ),
            CommunityRule(
                id = "p2",
                title = "Uso de la Información",
                description = "Utilizamos los datos exclusivamente para crear tu cuenta, identificarte dentro de la plataforma y hacer visible tu nombre ante los demás miembros al momento de compartir y autorizar tus apuntes."
            ),
            CommunityRule(
                id = "p3",
                title = "Protección de Datos",
                description = "Tus datos están protegidos por el equipo de ClassDrop. Nos comprometemos firmemente a no transferir ni compartir tu información con terceros."
            ),
            CommunityRule(
                id = "p4",
                title = "Tus Derechos",
                description = "Como titular de tus datos, tienes derecho a Acceder, Rectificar, Cancelar u Oponerte (Derechos ARCO) al uso de tu información en cualquier momento. Puedes ejercer estos derechos enviando una solicitud digital con tu credencial de la UPChiapas."
            ),
            CommunityRule(
                id = "p5",
                title = "¿Tienes dudas adicionales?",
                description = "Si tienes alguna duda o quieres ejercer tus derechos ARCO, comunícate con el equipo de The Sync a través de nuestro correo oficial de atención: soporte.classdrop@gmail.com"
            )
        )
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
        // This was inconsistent with Admin saving to Room.
        // For simple list retrieval without Flow:
        val json = prefs.getString("saved_privacy_rules", null)
        if (json == null) return getDefaultPrivacyRules()
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
