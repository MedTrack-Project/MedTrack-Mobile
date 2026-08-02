package com.medtrack.mobile.utils.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val payload = NotificationPayload(
            notificationId = inputData.getLong(NotificationPayload.KEY_NOTIFICATION_ID, -1).toInt(),
            medicationId = inputData.getLong(NotificationPayload.KEY_MEDICATION_ID, -1),
            name = inputData.getString(NotificationPayload.KEY_NAME) ?: return Result.failure(),
            activeIngredient = inputData.getString(NotificationPayload.KEY_ACTIVE_INGREDIENT).orEmpty(),
            time = inputData.getString(NotificationPayload.KEY_TIME) ?: return Result.failure(),
            date = inputData.getString(NotificationPayload.KEY_DATE) ?: return Result.failure(),
        )
        if (payload.notificationId <= 0 || payload.medicationId <= 0) return Result.failure()

        NotificationHelper.showNotification(
            context = applicationContext,
            medicamentoId = payload.medicationId,
            nome = payload.displayName(),
            horario = payload.time,
            dataAgendamento = payload.date,
            notificationId = payload.notificationId,
        )

        return Result.success()
    }
}
