package com.medtrack.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_queue")
data class ScanQueueItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,
    val status: String = "PENDENTE",
    val timestamp: Long = System.currentTimeMillis(),
)
