package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.error.InvalidRemoteResponseException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.RemoteRequestRejectedException
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
        repository.recoverInterrupted()
        val uploaded = mutableListOf<ProcessedScan>()
        var transientFailures = 0
        var permanentFailures = 0

        repository.pendingScans().forEach { scan ->
            if (scan.attemptCount >= MAX_ATTEMPTS - 1) {
                repository.markFailed(scan.id, "attempt_limit")
                permanentFailures++
                return@forEach
            }
            if (!repository.claim(scan.id)) return@forEach
            try {
                val medicamento = repository.uploadPending(scan)
                if (medicamento == null) {
                    repository.markFailed(scan.id, "medication_not_detected")
                    permanentFailures++
                } else {
                    repository.markUploaded(scan.id)
                    uploaded += ProcessedScan(scan, medicamento)
                }
            } catch (error: InvalidSessionException) {
                repository.markFailed(scan.id, "invalid_session")
                throw error
            } catch (_: RemoteRequestRejectedException) {
                repository.markFailed(scan.id, "request_rejected")
                permanentFailures++
            } catch (_: InvalidRemoteResponseException) {
                repository.markFailed(scan.id, "invalid_response")
                permanentFailures++
            } catch (_: Exception) {
                repository.markRetry(scan.id, "transient_failure")
                transientFailures++
            }
        }
        return OfflineScanProcessingResult(uploaded, transientFailures, permanentFailures)
    }

    companion object {
        const val MAX_ATTEMPTS = 5
    }
}

data class OfflineScanProcessingResult(
    val uploaded: List<ProcessedScan>,
    val transientFailures: Int,
    val permanentFailures: Int,
) {
    val shouldRetry: Boolean get() = transientFailures > 0
}
