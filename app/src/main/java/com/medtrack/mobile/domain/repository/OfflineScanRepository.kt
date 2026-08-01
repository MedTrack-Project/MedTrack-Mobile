package com.medtrack.mobile.domain.repository

import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.PendingScan

interface OfflineScanRepository {
    suspend fun pendingScans(): List<PendingScan>
    suspend fun uploadPending(scan: PendingScan): MedicamentoCapturadoDomain?
    suspend fun markCompleted(scanId: Int)
}
