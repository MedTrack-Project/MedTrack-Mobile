package com.medtrack.mobile.utils.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medtrack.mobile.data.local.source.MedicationLocalSource
import com.medtrack.mobile.data.mapper.local.toDomain
import com.medtrack.mobile.domain.service.MedicationScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RescheduleNotificationsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val local: MedicationLocalSource,
    private val scheduler: MedicationScheduler,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = try {
        local.medications().map { it.toDomain() }.forEach { scheduler.schedule(it) }
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    companion object {
        const val WORK_NAME = "reschedule_medication_notifications"
    }
}
