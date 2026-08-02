package com.medtrack.mobile.domain.repository

import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.PendingScan

interface OfflineScanRepository {
    suspend fun pendingScans(): List<PendingScan>
    suspend fun completedScans(): List<PendingScan>
    suspend fun claim(scanId: Int): Boolean
    suspend fun uploadPending(scan: PendingScan): MedicamentoCapturadoDomain?
    suspend fun markUploaded(scanId: Int)
    suspend fun markRetry(scanId: Int, reason: String)
    suspend fun markFailed(scanId: Int, reason: String)
    suspend fun markCompleted(scanId: Int)
    suspend fun deleteCompleted(scanId: Int): Boolean
    suspend fun recoverInterrupted()
}
