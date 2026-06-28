package com.classdrop.model

data class ModerationTask(
    val id: String,
    val fileName: String,
    val userName: String,
    val time: String,
    val flagReason: String,
    var status: ModerationStatus = ModerationStatus.PENDING
)

enum class ModerationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
