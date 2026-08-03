package com.medtrack.mobile.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val payload = NotificationPayload.fromIntent(intent) ?: return
        NotificationHelper.showNotification(
            context = context,
            medicamentoId = payload.medicationId,
            nome = payload.displayName(),
            horario = payload.time,
            dataAgendamento = payload.date,
            notificationId = payload.notificationId,
            urgent = true,
        )
    }
}

data class NotificationPayload(
    val notificationId: Int,
    val medicationId: Long,
    val name: String,
    val activeIngredient: String,
    val time: String,
    val date: String,
) {
    fun displayName(): String = if (
        name.equals("MEDICAMENTO GENERICO", ignoreCase = true) ||
        name.equals("MEDICAMENTO GENÉRICO", ignoreCase = true)
    ) {
        activeIngredient.ifBlank { name }
    } else {
        name
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "notificationId"
        const val KEY_MEDICATION_ID = "medicamentoId"
        const val KEY_NAME = "nome"
        const val KEY_ACTIVE_INGREDIENT = "compostoAtivo"
        const val KEY_TIME = "horario"
        const val KEY_DATE = "dataAgendamento"

        fun fromIntent(intent: Intent): NotificationPayload? {
            val medicationId = intent.getLongExtra(KEY_MEDICATION_ID, -1)
            val name = intent.getStringExtra(KEY_NAME)
            val time = intent.getStringExtra(KEY_TIME)
            return if (medicationId > 0 && name != null && time != null) {
                NotificationPayload(
                    notificationId = intent.getLongExtra(KEY_NOTIFICATION_ID, medicationId).toInt(),
                    medicationId = medicationId,
                    name = name,
                    activeIngredient = intent.getStringExtra(KEY_ACTIVE_INGREDIENT).orEmpty(),
                    time = time,
                    date = intent.getStringExtra(KEY_DATE) ?: LocalDate.now().toString(),
                )
            } else {
                null
            }
        }
    }
}
