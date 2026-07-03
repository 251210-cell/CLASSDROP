package com.classdrop.model

data class ModerationTask(
    val id: String,
    val fileName: String,
    val userName: String,
    val time: String,
    val flagReason: String,
    val fileUrl: String? = null,
    val fileType: String? = "PDF",
    var status: ModerationStatus = ModerationStatus.PENDING
)

enum class ModerationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
