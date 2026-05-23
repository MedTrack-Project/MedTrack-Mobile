package com.example.piec_1.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.domain.usecase.getDatesBetween
import com.example.piec_1.domain.usecase.horariosDoDia
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
    @param:ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun agendarNotificacao(medicamento: MedicamentoDomain) {
        try {
            val horarios = medicamento.frequenciaUso.horariosDoDia()

            if (medicamento.frequenciaUso.usoContinuo) {
                horarios.distinct().forEach { horario ->
                    scheduleDailyNotification(medicamento, horario)
                }
            } else {
                val startDate = medicamento.frequenciaUso.dataInicio ?: LocalDate.now()
                val endDate = medicamento.frequenciaUso.dataTermino ?: startDate

                getDatesBetween(startDate, endDate)
                    .filter { !it.isBefore(LocalDate.now()) }
                    .forEach { dataAgendamento ->
                    horarios.forEach { horario ->
                        scheduleSingleNotification(medicamento, horario, dataAgendamento)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Notification", "Erro ao agendar: ${e.message}")
            scheduleUsingWorkManager(medicamento)
        }
    }

    fun scheduleUsingWorkManager(medicamento: MedicamentoDomain) {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        medicamento.frequenciaUso.horariosDoDia().forEach { horario ->
            val inputData = Data.Builder()
                .putLong("medicamentoId", medicamento.id)
                .putString("nome", medicamento.nome)
                .putString("compostoAtivo", medicamento.compostoAtivo)
                .putString("horario", horario.toString())
                .putString("imagemUrl", medicamento.imagemUrl)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(calculateDelay(horario), TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    private fun calculateDelay(horario: LocalTime): Long {
        return nextTriggerAt(horario) - System.currentTimeMillis()
    }

    private fun scheduleDailyNotification(medicamento: MedicamentoDomain, horario: LocalTime) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("medicamentoId", medicamento.id)
            putExtra("nome", medicamento.nome)
            putExtra("compostoAtivo", medicamento.compostoAtivo)
            putExtra("horario", horario.toString())
            putExtra("imagemUrl", medicamento.imagemUrl)
            putExtra("isContinuo", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${medicamento.id}_${horario}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerAt(horario),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun scheduleSingleNotification(
        medicamento: MedicamentoDomain,
        horario: LocalTime,
        dataAgendamento: LocalDate
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("medicamentoId", medicamento.id)
            putExtra("nome", medicamento.nome)
            putExtra("compostoAtivo", medicamento.compostoAtivo)
            putExtra("horario", horario.toString())
            putExtra("imagemUrl", medicamento.imagemUrl)
            putExtra("isContinuo", false)
            putExtra("dataAgendamento", dataAgendamento.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${medicamento.id}_${horario}_${dataAgendamento}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = dataAgendamento
            .atTime(horario)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }

    private fun nextTriggerAt(horario: LocalTime): Long {
        val now = LocalDateTime.now()
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
