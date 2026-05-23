package com.example.piec_1.utils.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val medicamentoId = inputData.getLong("medicamentoId", -1)
        var nome = inputData.getString("nome") ?: return Result.failure()
        val compostoAtivo = inputData.getString("compostoAtivo").orEmpty()
        val horario = inputData.getString("horario") ?: return Result.failure()
        val imagemUrl = inputData.getString("imagemUrl")

        if (nome.equals("MEDICAMENTO GENERICO", ignoreCase = true) ||
            nome.equals("MEDICAMENTO GENÉRICO", ignoreCase = true)
        ) {
            nome = compostoAtivo.ifBlank { nome }
        }

        NotificationHelper.showNotification(applicationContext, medicamentoId, nome, horario, imagemUrl)

        return Result.success()
    }
}
