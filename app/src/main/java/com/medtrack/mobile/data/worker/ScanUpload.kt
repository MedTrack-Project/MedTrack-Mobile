package com.medtrack.mobile.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.medtrack.mobile.MainActivity
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.usecase.ProcessOfflineScanQueueUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File

class ScanUpload(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "ScanUpload"
    }

    private val processQueue: ProcessOfflineScanQueueUseCase by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ScanUploadEntryPoint::class.java,
        ).processOfflineScanQueue()
    }

    override suspend fun doWork(): Result = try {
        val result = processQueue()
        result.completed.forEach { processed ->
            enviarNotificacaoComDados(processed.medicamento)
            val file = File(processed.pendingScan.image.value.toUri().path.orEmpty())
            if (!file.delete()) Log.w(TAG, "Arquivo processado nao pode ser removido")
        }
        if (result.shouldRetry) Result.retry() else Result.success()
    } catch (_: InvalidSessionException) {
        Result.failure()
    } catch (_: Exception) {
        Log.e(TAG, "Erro ao processar fila offline")
        Result.retry()
    }

    private fun enviarNotificacaoComDados(medicamento: MedicamentoCapturadoDomain) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "offline_scan_channel"

        val channel = NotificationChannel(
            channelId,
            "Scans Offline",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        notificationManager.createNotificationChannel(channel)

        val medicamentoJson = Gson().toJson(medicamento)
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            action = "OPEN_CONFIRMATION"
            putExtra("medicamento_json", medicamentoJson)
            putExtra("navigate_to_confirmation", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Medicamento Processado")
            .setContentText(medicamento.nome)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        """
                        ${medicamento.nome}
                        ${medicamento.compostoAtivo}
                        Dosagem: ${medicamento.dosagem}
                        Quantidade: ${medicamento.quantidade}
                        Validade: ${medicamento.validade?.ifBlank { "N/A" } ?: "N/A"}
                        
                        Clique para confirmar ou editar as informacoes
                        """.trimIndent(),
                    ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ScanUploadEntryPoint {
        fun processOfflineScanQueue(): ProcessOfflineScanQueueUseCase
    }
}
