package com.classdrop.model

data class CommentReport(
    val id: String,
    val reporterName: String,
    val reportedUserName: String,
    val time: String,
    val dislikes: String, // e.g., "5/5"
    val contextTitle: String, // e.g., "Dudas Práctica 3 - Cálculo II"
    val commentContent: String,
    var status: ReportStatus = ReportStatus.PENDING
)

enum class ReportStatus {
    PENDING,
    REMOVED,
    KEPT
}
