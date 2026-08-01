package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.ProcessedScan
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.repository.ScanRepository
import javax.inject.Inject

class ScanMedicationUseCase @Inject constructor(private val repository: ScanRepository) {
    suspend operator fun invoke(image: ImageReference): MedicamentoCapturadoDomain? = repository.scan(image)
}

class QueueOfflineScanUseCase @Inject constructor(private val repository: ScanRepository) {
    suspend operator fun invoke(image: ImageReference) = repository.enqueue(image)
}

class ProcessOfflineScanQueueUseCase @Inject constructor(private val repository: OfflineScanRepository) {
    suspend operator fun invoke(): OfflineScanProcessingResult {
        val completed = mutableListOf<ProcessedScan>()
        var shouldRetry = false

        repository.pendingScans().forEach { scan ->
            val medicamento = runCatching { repository.uploadPending(scan) }
                .getOrElse {
                    if (it is InvalidSessionException) throw it
                    shouldRetry = true
                    null
                }
            if (medicamento == null) {
                shouldRetry = true
            } else {
                repository.markCompleted(scan.id)
                completed += ProcessedScan(scan, medicamento)
            }
        }
        return OfflineScanProcessingResult(completed, shouldRetry)
    }
}

data class OfflineScanProcessingResult(val completed: List<ProcessedScan>, val shouldRetry: Boolean)
