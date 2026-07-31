package com.medtrack.mobile.utils.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.medtrack.mobile.MainActivity
import com.medtrack.mobile.R
import com.medtrack.mobile.ui.navigation.AppRoutes
import com.medtrack.mobile.utils.formatarHorario
import java.net.URL
import java.time.LocalDate

object NotificationHelper {

    fun showNotification(
        context: Context,
        medicamentoId: Long,
        nome: String,
        horario: String,
        imagemUrl: String? = null,
        dataAgendamento: String? = null,
        notificationId: Int? = null,
    ) {
        createNotificationChannel(context)

        val horarioFormatado = formatarHorario(horario)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = AppRoutes.doseHorarioDeepLink(
                medicamentoId = medicamentoId,
                data = dataAgendamento ?: LocalDate.now().toString(),
                horario = horarioFormatado,
            ).toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicamentoId", medicamentoId)
            putExtra("horario", horarioFormatado)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medicamentoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("Nao esqueca de tomar seu medicamento $nome no horario: $horarioFormatado")
            .setBigContentTitle("Hora do remedio!")
            .setSummaryText("MedTrack - Lembrete")

        val notification = NotificationCompat.Builder(context, "medicamento_channel")
            .setContentTitle("Hora do remedio: $nome")
            .setContentText("Horario: $horarioFormatado")
            .setSmallIcon(R.drawable.medtrack_white_icon)
            .setLargeIcon(loadMedicationBitmap(context, imagemUrl))
            .setStyle(bigTextStyle)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.notification_color))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE,
        ) as NotificationManager
        notificationManager.notify(notificationId ?: medicamentoId.toInt(), notification)
    }

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "medicamento_channel",
            "Lembretes de Medicamentos",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notificacoes para lembrar de tomar medicamentos"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            vibrationPattern = longArrayOf(0, 200, 100, 200)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun loadMedicationBitmap(context: Context, imagemUrl: String?): Bitmap {
        if (imagemUrl.isNullOrBlank()) return defaultBitmap(context)

        return runCatching {
            URL(imagemUrl).openStream().use(BitmapFactory::decodeStream)
        }.getOrNull() ?: defaultBitmap(context)
    }

    private fun defaultBitmap(context: Context): Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.medtrack_white_icon)
}
