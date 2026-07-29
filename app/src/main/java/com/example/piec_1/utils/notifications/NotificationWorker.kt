package com.example.piec_1.utils.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val notificationId = inputData.getLong("notificationId", -1)
        val medicamentoId = inputData.getLong("medicamentoId", -1)
        var nome = inputData.getString("nome") ?: return Result.failure()
        val compostoAtivo = inputData.getString("compostoAtivo").orEmpty()
        val horario = inputData.getString("horario") ?: return Result.failure()
        val imagemUrl = inputData.getString("imagemUrl")
        val dataAgendamento = inputData.getString("dataAgendamento")

        if (nome.equals("MEDICAMENTO GENERICO", ignoreCase = true) ||
            nome.equals("MEDICAMENTO GENÉRICO", ignoreCase = true)
        ) {
            nome = compostoAtivo.ifBlank { nome }
        }

        NotificationHelper.showNotification(
            context = applicationContext,
            medicamentoId = medicamentoId,
            nome = nome,
            horario = horario,
            imagemUrl = imagemUrl,
            dataAgendamento = dataAgendamento,
            notificationId = notificationId.takeIf { it > 0 }?.toInt(),
        )

        return Result.success()
    }
}
