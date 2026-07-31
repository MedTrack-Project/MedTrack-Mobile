package com.medtrack.mobile.domain.service

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
import com.medtrack.mobile.MainActivity
import com.medtrack.mobile.data.repository.ScanRepository
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.utils.exceptions.TokenNaoEncontradoException
import com.google.gson.Gson
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File

class ScanUpload(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "ScanUpload"
        private const val STATUS_CONCLUIDO = "CONCLUIDO"
    }

    private val repository: ScanRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ScanUploadEntryPoint::class.java,
        ).scanRepository()
    }

    override suspend fun doWork(): Result {
        val pendingScans = repository.getPendingScans()

        if (pendingScans.isEmpty()) {
            return Result.success()
        }

        var allSuccess = true

        pendingScans.forEach { scan ->
            try {
                val file = File(scan.imagePath.toUri().path.orEmpty())

                if (!file.exists()) {
                    Log.e(TAG, "Arquivo de scan nao encontrado")
                    allSuccess = false
                    return@forEach
                }

                val medicamento = repository.uploadScanPendente(file)

                if (medicamento != null) {
                    repository.updateScanStatus(scan.id, STATUS_CONCLUIDO)
                    enviarNotificacaoComDados(medicamento)
                    file.delete()
                } else {
                    allSuccess = false
                }
            } catch (_: TokenNaoEncontradoException) {
                return Result.failure()
            } catch (_: Exception) {
                Log.e(TAG, "Erro ao processar scan pendente")
                allSuccess = false
            }
        }

        return if (allSuccess) Result.success() else Result.retry()
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
        fun scanRepository(): ScanRepository
    }
}
