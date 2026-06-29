package com.classdrop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.classdrop.data.local.dao.PrivacyRuleDao
import com.classdrop.data.local.dao.SubjectDao
import com.classdrop.data.local.entity.PrivacyRuleEntity
import com.classdrop.data.local.entity.SubjectEntity

@Database(entities = [SubjectEntity::class, PrivacyRuleEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun privacyRuleDao(): PrivacyRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "classdrop_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
