package com.medtrack.mobile.utils.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val payload = inputPayload()
        return if (payload == null) {
            Result.failure()
        } else {
            NotificationHelper.showNotification(
                context = applicationContext,
                medicamentoId = payload.medicationId,
                nome = payload.displayName(),
                horario = payload.time,
                dataAgendamento = payload.date,
                notificationId = payload.notificationId,
            )
            Result.success()
        }
    }

    private fun inputPayload(): NotificationPayload? {
        val notificationId = inputData.getLong(NotificationPayload.KEY_NOTIFICATION_ID, -1).toInt()
        val medicationId = inputData.getLong(NotificationPayload.KEY_MEDICATION_ID, -1)
        val name = inputData.getString(NotificationPayload.KEY_NAME)
        val time = inputData.getString(NotificationPayload.KEY_TIME)
        val date = inputData.getString(NotificationPayload.KEY_DATE)
        val hasValidIdentifiers = notificationId > 0 && medicationId > 0
        val hasRequiredText = name != null && time != null && date != null
        return if (hasValidIdentifiers && hasRequiredText) {
            NotificationPayload(
                notificationId = notificationId,
                medicationId = medicationId,
                name = requireNotNull(name),
                activeIngredient = inputData.getString(NotificationPayload.KEY_ACTIVE_INGREDIENT).orEmpty(),
                time = requireNotNull(time),
                date = requireNotNull(date),
            )
        } else {
            null
        }
    }
}
