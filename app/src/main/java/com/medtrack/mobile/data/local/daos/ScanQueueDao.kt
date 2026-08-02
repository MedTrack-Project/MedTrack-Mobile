package com.medtrack.mobile.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtrack.mobile.data.local.entity.ScanQueueItem
import com.medtrack.mobile.data.local.entity.ScanQueueStatus

@Dao
interface ScanQueueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ScanQueueItem): Long

    @Query(
        "SELECT * FROM scan_queue WHERE status IN ('PENDING', 'RETRY') " +
            "ORDER BY timestamp ASC",
    )
    suspend fun getProcessableScans(): List<ScanQueueItem>

    @Query("SELECT * FROM scan_queue WHERE status = 'COMPLETED' ORDER BY updatedAt ASC")
    suspend fun getCompletedScans(): List<ScanQueueItem>

    @Query(
        "UPDATE scan_queue SET status = 'PROCESSING', attemptCount = attemptCount + 1, " +
            "updatedAt = :updatedAt, lastError = NULL " +
            "WHERE id = :id AND status IN ('PENDING', 'RETRY')",
    )
    suspend fun claim(id: Int, updatedAt: Long): Int

    @Query(
        "UPDATE scan_queue SET status = :status, updatedAt = :updatedAt, lastError = :lastError WHERE id = :id",
    )
    suspend fun updateState(id: Int, status: ScanQueueStatus, updatedAt: Long, lastError: String? = null)

    @Query("SELECT * FROM scan_queue WHERE id = :id")
    suspend fun getById(id: Int): ScanQueueItem?

    @Query("DELETE FROM scan_queue WHERE id = :id AND status = 'COMPLETED'")
    suspend fun deleteCompleted(id: Int): Int

    @Query(
        "UPDATE scan_queue SET status = 'RETRY', updatedAt = :now, lastError = 'interrupted' " +
            "WHERE status = 'PROCESSING' AND updatedAt < :staleBefore",
    )
    suspend fun recoverStaleProcessing(staleBefore: Long, now: Long): Int
}
