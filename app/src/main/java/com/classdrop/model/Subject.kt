package com.classdrop.model

data class Subject(
    val id: String,
    val name: String,
    val fileCount: Int,
    val iconRes: Int,
    val iconBgColor: String,
    val iconTintColor: String,
    val cuatrimestre: String = "1 Cuatrimestre"
)
