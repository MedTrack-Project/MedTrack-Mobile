package com.medtrack.mobile.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.medtrack.mobile.data.local.daos.NotificacaoDao
import com.medtrack.mobile.data.local.entity.NotificacaoEntity
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.service.MedicationScheduler
import com.medtrack.mobile.domain.time.AppClock
import com.medtrack.mobile.domain.usecase.getDatesBetween
import com.medtrack.mobile.domain.usecase.horariosDoDia
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificacaoDao: NotificacaoDao,
    private val clock: AppClock,
) : MedicationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    override suspend fun schedule(medicamento: MedicamentoDomain) {
        NotificationHelper.createNotificationChannel(context)
        notificacaoDao.deleteByMedicamentoId(medicamento.id)

        val horarios = medicamento.frequenciaUso.horariosDoDia().distinct()
        if (horarios.isEmpty()) return

        if (medicamento.frequenciaUso.usoContinuo) {
            horarios.forEach { horario ->
                scheduleDailyNotification(medicamento, horario)
            }
            return
        }

        val startDate = medicamento.frequenciaUso.dataInicio ?: clock.localDate()
        val endDate = medicamento.frequenciaUso.dataTermino ?: startDate

        getDatesBetween(startDate, endDate)
            .filter { !it.isBefore(clock.localDate()) }
            .forEach { dataAgendamento ->
                horarios.forEach { horario ->
                    scheduleSingleNotification(medicamento, horario, dataAgendamento)
                }
            }
    }

    private suspend fun scheduleDailyNotification(medicamento: MedicamentoDomain, horario: LocalTime) {
        val triggerAt = nextTriggerAt(horario)
        val dataAgendamento = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(triggerAt),
            ZoneId.systemDefault(),
        ).toLocalDate()

        val notificationId = saveNotification(medicamento, horario, dataAgendamento)
        scheduleAlarm(
            medicamento,
            horario,
            dataAgendamento,
            notificationId,
            triggerAt,
            repeatsDaily = true,
        )
        scheduleWork(medicamento, horario, dataAgendamento, notificationId, triggerAt)
    }

    private suspend fun scheduleSingleNotification(
        medicamento: MedicamentoDomain,
        horario: LocalTime,
        dataAgendamento: LocalDate,
    ) {
        val triggerAt = dataAgendamento
            .atTime(horario)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerAt < clock.instant().toEpochMilli()) return

        val notificationId = saveNotification(medicamento, horario, dataAgendamento)
        scheduleAlarm(
            medicamento,
            horario,
            dataAgendamento,
            notificationId,
            triggerAt,
            repeatsDaily = false,
        )
        scheduleWork(medicamento, horario, dataAgendamento, notificationId, triggerAt)
    }

    private suspend fun saveNotification(
        medicamento: MedicamentoDomain,
        horario: LocalTime,
        dataAgendamento: LocalDate,
    ): Long = notificacaoDao.insert(
        NotificacaoEntity(
            medicamentoId = medicamento.id,
            horario = horario.toString(),
            dataAgendamento = dataAgendamento.toString(),
            exibida = false,
        ),
    )

    private fun scheduleAlarm(
        medicamento: MedicamentoDomain,
        horario: LocalTime,
        dataAgendamento: LocalDate,
        notificationId: Long,
        triggerAt: Long,
        repeatsDaily: Boolean,
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.toInt(),
            notificationIntent(medicamento, horario, dataAgendamento, notificationId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (repeatsDaily) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                AlarmManager.INTERVAL_DAY,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun scheduleWork(
        medicamento: MedicamentoDomain,
        horario: LocalTime,
        dataAgendamento: LocalDate,
        notificationId: Long,
        triggerAt: Long,
    ) {
        val delayMillis = (triggerAt - clock.instant().toEpochMilli()).coerceAtLeast(0)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(notificationData(medicamento, horario, dataAgendamento, notificationId))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "notification_$notificationId",
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }

    private fun notificationIntent(
        medicamento: MedicamentoDomain,
        horario: LocalTime,
        dataAgendamento: LocalDate,
        notificationId: Long,
    ): Intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("notificationId", notificationId)
        putExtra("medicamentoId", medicamento.id)
        putExtra("nome", medicamento.nome)
        putExtra("compostoAtivo", medicamento.compostoAtivo)
        putExtra("horario", horario.toString())
        putExtra("imagemUrl", medicamento.imagemUrl)
        putExtra("dataAgendamento", dataAgendamento.toString())
    }

    private fun notificationData(
        medicamento: MedicamentoDomain,
        horario: LocalTime,
        dataAgendamento: LocalDate,
        notificationId: Long,
    ): Data = Data.Builder()
        .putLong("notificationId", notificationId)
        .putLong("medicamentoId", medicamento.id)
        .putString("nome", medicamento.nome)
        .putString("compostoAtivo", medicamento.compostoAtivo)
        .putString("horario", horario.toString())
        .putString("imagemUrl", medicamento.imagemUrl)
        .putString("dataAgendamento", dataAgendamento.toString())
        .build()

    private fun nextTriggerAt(horario: LocalTime): Long {
        val now = clock.localDateTime()
        var dateTime = now.toLocalDate().atTime(horario)

        if (dateTime.isBefore(now)) {
            dateTime = dateTime.plusDays(1)
        }

        return dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
