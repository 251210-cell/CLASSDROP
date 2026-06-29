package com.classdrop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_rules")
data class PrivacyRuleEntity(
    @PrimaryKey val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val iconResName: String,
    val lastEdited: String,
    val isActive: Boolean,
    val adminNote: String
)
