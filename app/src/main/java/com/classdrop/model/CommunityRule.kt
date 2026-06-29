package com.classdrop.model

import java.io.Serializable

data class CommunityRule(
    val id: String = "",
    val number: Int = 0,
    val title: String = "",
    val description: String = "",
    val iconResName: String = "ic_status_shield",
    val lastEdited: String = "",
    val isActive: Boolean = true,
    val adminNote: String = ""
) : Serializable
