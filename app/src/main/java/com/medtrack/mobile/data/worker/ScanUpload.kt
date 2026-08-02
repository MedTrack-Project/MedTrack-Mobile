package com.medtrack.mobile.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.usecase.ProcessOfflineScanQueueUseCase
import com.medtrack.mobile.utils.notifications.OfflineScanNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScanUpload @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val processQueue: ProcessOfflineScanQueueUseCase,
    private val repository: OfflineScanRepository,
    private val notifier: OfflineScanNotifier,
    private val fileCleaner: ScanFileCleanup,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = try {
        repository.completedScans().forEach { completed ->
            if (fileCleaner.delete(completed.image)) repository.deleteCompleted(completed.id)
        }
        val result = processQueue()
        result.uploaded.forEach { processed ->
            notifier.show(processed.pendingScan.id, processed.medicamento)
            repository.markCompleted(processed.pendingScan.id)
            if (fileCleaner.delete(processed.pendingScan.image)) {
                repository.deleteCompleted(processed.pendingScan.id)
            }
        }
        when {
            result.shouldRetry && runAttemptCount + 1 < ProcessOfflineScanQueueUseCase.MAX_ATTEMPTS -> Result.retry()
            result.shouldRetry -> Result.failure()
            else -> Result.success()
        }
    } catch (_: InvalidSessionException) {
        Result.failure()
    } catch (_: Exception) {
        if (runAttemptCount + 1 < ProcessOfflineScanQueueUseCase.MAX_ATTEMPTS) Result.retry() else Result.failure()
    }
}
