package com.medtrack.mobile.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.medtrack.mobile.data.local.entity.ScanQueueItem

@Dao
interface ScanQueueDao {
    @Insert
    suspend fun insert(item: ScanQueueItem)

    @Query("SELECT * FROM scan_queue WHERE status = 'PENDENTE' ORDER BY timestamp ASC")
    suspend fun getPendingScans(): List<ScanQueueItem>

    @Query("UPDATE scan_queue SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}
