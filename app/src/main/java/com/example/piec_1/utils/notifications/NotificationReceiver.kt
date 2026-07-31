package com.example.piec_1.utils.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.piec_1.MainActivity
import com.example.piec_1.R
import com.example.piec_1.ui.navigation.AppRoutes
import com.example.piec_1.utils.formatarHorario
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val notificationId = intent.getLongExtra("notificationId", -1)
        val medicamentoId = intent.getLongExtra("medicamentoId", -1)
        var nome = intent.getStringExtra("nome")
        val horarioOriginal = intent.getStringExtra("horario")
        val compostoAtivo = intent.getStringExtra("compostoAtivo")
        val imagemUrl = intent.getStringExtra("imagemUrl")
        val dataAgendamento = intent.getStringExtra("dataAgendamento") ?: LocalDate.now().toString()

        if (medicamentoId == -1L ||
            nome == null ||
            horarioOriginal == null ||
            compostoAtivo == null
        ) {
            pendingResult.finish()
            return
        }

        val horario = formatarHorario(horarioOriginal)

        if (
            nome.equals("MEDICAMENTO GENERICO", ignoreCase = true) ||
            nome.equals("MEDICAMENTO GENÉRICO", ignoreCase = true)
        ) {
            nome = compostoAtivo
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = AppRoutes.doseHorarioDeepLink(
                        medicamentoId = medicamentoId,
                        data = dataAgendamento,
                        horario = horario,
                    ).toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    medicamentoId.toInt(),
                    deepLinkIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .bigText("São $horario, está na hora de tomar $nome")
                    .setBigContentTitle("Hora do remedio!")
                    .setSummaryText("MedTrack - Lembrete")

                NotificationCompat.Builder(context, "medicamento_channel")
                    .setContentTitle("Hora de tomar $nome")
                    .setContentText("Horario: $horario")
                    .setSmallIcon(R.drawable.medtrack_white_icon)
                    .setLargeIcon(NotificationHelper.loadMedicationBitmap(context, imagemUrl))
                    .setStyle(bigTextStyle)
                    .setColorized(true)
                    .setColor(ContextCompat.getColor(context, R.color.notification_color))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setContentIntent(fullScreenPendingIntent)
                    .setAutoCancel(true)
                    .build()
                    .let { notification ->
                        context.getSystemService(NotificationManager::class.java).notify(
                            notificationId.takeIf { it > 0 }?.toInt() ?: medicamentoId.toInt(),
                            notification,
                        )
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
