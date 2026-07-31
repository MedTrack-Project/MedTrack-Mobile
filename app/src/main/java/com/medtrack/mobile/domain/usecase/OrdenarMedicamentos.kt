package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.model.DoseStatus
import com.medtrack.mobile.domain.model.FrequenciaUsoDomain
import com.medtrack.mobile.domain.model.FrequenciaUsoTipo
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.MedicationItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
fun organizeMedicationsByDay(
    medicamentos: List<MedicamentoDomain>,
    currentDate: LocalDate,
    maxDaysToShow: Int = 7,
    confirmedDoseKeys: Set<String> = emptySet(),
    now: LocalDateTime = LocalDateTime.now(),
): Map<LocalDate, List<MedicationItem>> {
    val datesToShow = getDatesBetween(currentDate, currentDate.plusDays(maxDaysToShow.toLong() - 1))
    val result = mutableMapOf<LocalDate, MutableList<MedicationItem>>()

    medicamentos.forEach { medicamento ->
        medicamento.toScheduledMedicationItems(
            datesToShow,
            confirmedDoseKeys,
            now,
        ).forEach { scheduledItem ->
            result.getOrPut(scheduledItem.date) { mutableListOf() }.add(scheduledItem.item)
        }
    }

    result.forEach { (_, items) ->
        items.sortBy { LocalTime.parse(it.horario) }
    }

    return result.toSortedMap()
}

fun MedicamentoDomain.toScheduledMedicationItems(
    datesToShow: List<LocalDate>,
    confirmedDoseKeys: Set<String> = emptySet(),
    now: LocalDateTime = LocalDateTime.now(),
): List<ScheduledMedicationItem> {
    val ehGenerico = nome.equals("MEDICAMENTO GENERICO", ignoreCase = true) ||
        nome.equals("MEDICAMENTO GENÉRICO", ignoreCase = true)
    val nomeExibicao = if (ehGenerico) compostoAtivo else nome
    val activeDates = datesToShow.filter { frequenciaUso.isActiveOn(it) }
    val horarios = frequenciaUso.horariosDoDia()

    return activeDates.flatMap { date ->
        horarios.map { horario ->
            val horarioFormatado = horario.format(timeFormatter)
            val doseKey = doseKey(id, date, horarioFormatado)
            ScheduledMedicationItem(
                date = date,
                item = MedicationItem(
                    id = doseKey,
                    medicamentoId = id,
                    date = date,
                    nomeExibicao = nomeExibicao,
                    horario = horarioFormatado,
                    dosagem = dosagem,
                    imagemUrl = imagemUrl,
                    isContinuous = frequenciaUso.usoContinuo,
                    isGenerico = ehGenerico,
                    status = resolveDoseStatus(id, date, horario, confirmedDoseKeys, now),
                ),
            )
        }
    }
}

fun FrequenciaUsoDomain.horariosDoDia(): List<LocalTime> = when (frequenciaUsoTipo) {
    FrequenciaUsoTipo.HORARIOS_ESPECIFICOS -> horariosEspecificos.sorted()
    FrequenciaUsoTipo.INTERVALO_ENTRE_DOSES -> horariosPorIntervalo()
}

private fun FrequenciaUsoDomain.horariosPorIntervalo(): List<LocalTime> {
    val inicio = primeiroHorario ?: return emptyList()
    val intervalo = intervaloHoras?.takeIf { it > 0 } ?: return emptyList()
    val totalHorarios = (24 / intervalo).coerceAtLeast(1)

    return generateSequence(inicio) { it.plusHours(intervalo.toLong()) }
        .take(totalHorarios)
        .distinct()
        .sorted()
        .toList()
}

private fun FrequenciaUsoDomain.isActiveOn(date: LocalDate): Boolean {
    val startsBeforeOrOnDate = dataInicio?.let { !date.isBefore(it) } ?: true
    val endsAfterOrOnDate = dataTermino?.let { !date.isAfter(it) } ?: usoContinuo

    return startsBeforeOrOnDate && endsAfterOrOnDate
}

fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var currentDate = startDate

    while (!currentDate.isAfter(endDate)) {
        dates.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }

    return dates
}

data class ScheduledMedicationItem(val date: LocalDate, val item: MedicationItem)

fun doseKey(medicamentoId: Long, date: LocalDate, horario: String): String = "${medicamentoId}_${date}_$horario"

fun resolveDoseStatus(
    medicamentoId: Long,
    date: LocalDate,
    horario: LocalTime,
    confirmedDoseKeys: Set<String>,
    now: LocalDateTime = LocalDateTime.now(),
): DoseStatus {
    val horarioFormatado = horario.format(timeFormatter)
    if (doseKey(medicamentoId, date, horarioFormatado) in confirmedDoseKeys) {
        return DoseStatus.CONFIRMED
    }

    val scheduledAt = date.atTime(horario)
    return when {
        now.isBefore(scheduledAt) -> DoseStatus.FUTURE
        now.isEqual(scheduledAt) -> DoseStatus.AVAILABLE
        else -> DoseStatus.LATE
    }
}
