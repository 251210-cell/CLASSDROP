package com.classdrop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val fileCount: Int,
    val iconRes: Int,
    val iconBgColor: String,
    val iconTintColor: String,
    val cuatrimestre: String
)
