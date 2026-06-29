package com.classdrop.data.local.dao

import androidx.room.*
import com.classdrop.data.local.entity.PrivacyRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacyRuleDao {
    @Query("SELECT * FROM privacy_rules")
    fun getAllRules(): Flow<List<PrivacyRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<PrivacyRuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: PrivacyRuleEntity)

    @Delete
    suspend fun deleteRule(rule: PrivacyRuleEntity)

    @Query("DELETE FROM privacy_rules")
    suspend fun deleteAllRules()
}
