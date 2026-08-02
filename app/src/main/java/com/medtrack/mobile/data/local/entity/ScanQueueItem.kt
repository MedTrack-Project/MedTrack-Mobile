package com.medtrack.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan_queue",
    indices = [Index(value = ["idempotencyKey"], unique = true)],
)
data class ScanQueueItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,
    val idempotencyKey: String,
    val status: ScanQueueStatus = ScanQueueStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val updatedAt: Long = timestamp,
    val lastError: String? = null,
)

enum class ScanQueueStatus {
    PENDING,
    PROCESSING,
    UPLOADED,
    COMPLETED,
    RETRY,
    FAILED,
}
