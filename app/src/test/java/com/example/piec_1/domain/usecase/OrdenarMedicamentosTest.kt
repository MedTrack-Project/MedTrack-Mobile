package com.example.piec_1.domain.usecase

import com.example.piec_1.domain.model.DoseStatus
import com.example.piec_1.domain.model.FrequenciaUsoDomain
import com.example.piec_1.domain.model.FrequenciaUsoTipo
import com.example.piec_1.domain.model.MedicamentoDomain
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdenarMedicamentosTest {

    @Test
    fun `getDatesBetween returns inclusive date range`() {
        val dates = getDatesBetween(
            startDate = LocalDate.parse("2026-05-25"),
            endDate = LocalDate.parse("2026-05-27"),
        )

        assertEquals(
            listOf(
                LocalDate.parse("2026-05-25"),
                LocalDate.parse("2026-05-26"),
                LocalDate.parse("2026-05-27"),
            ),
            dates,
        )
    }

    @Test
    fun `horariosDoDia sorts specific times`() {
        val frequency = frequenciaHorariosEspecificos(
            horarios = listOf(LocalTime.of(20, 0), LocalTime.of(8, 0), LocalTime.of(14, 30)),
        )

        assertEquals(
            listOf(LocalTime.of(8, 0), LocalTime.of(14, 30), LocalTime.of(20, 0)),
            frequency.horariosDoDia(),
        )
    }

    @Test
    fun `horariosDoDia creates daily times from interval`() {
        val frequency = FrequenciaUsoDomain(
            frequenciaUsoTipo = FrequenciaUsoTipo.INTERVALO_ENTRE_DOSES,
            usoContinuo = true,
            horariosEspecificos = emptyList(),
            intervaloHoras = 8,
            primeiroHorario = LocalTime.of(6, 0),
            dataInicio = null,
            dataTermino = null,
        )

        assertEquals(
            listOf(LocalTime.of(6, 0), LocalTime.of(14, 0), LocalTime.of(22, 0)),
            frequency.horariosDoDia(),
        )
    }

    @Test
    fun `resolveDoseStatus returns confirmed before checking schedule time`() {
        val date = LocalDate.parse("2026-05-25")
        val confirmed = setOf(doseKey(1, date, "08:00"))

        val status = resolveDoseStatus(
            medicamentoId = 1,
            date = date,
            horario = LocalTime.of(8, 0),
            confirmedDoseKeys = confirmed,
            now = LocalDateTime.parse("2026-05-25T07:00:00"),
        )

        assertEquals(DoseStatus.CONFIRMED, status)
    }

    @Test
    fun `resolveDoseStatus classifies future available and late doses`() {
        val date = LocalDate.parse("2026-05-25")

        assertEquals(
            DoseStatus.FUTURE,
            resolveDoseStatus(
                1,
                date,
                LocalTime.of(10, 0),
                emptySet(),
                LocalDateTime.parse("2026-05-25T09:59:00"),
            ),
        )
        assertEquals(
            DoseStatus.AVAILABLE,
            resolveDoseStatus(
                1,
                date,
                LocalTime.of(10, 0),
                emptySet(),
                LocalDateTime.parse("2026-05-25T10:00:00"),
            ),
        )
        assertEquals(
            DoseStatus.LATE,
            resolveDoseStatus(
                1,
                date,
                LocalTime.of(10, 0),
                emptySet(),
                LocalDateTime.parse("2026-05-25T10:01:00"),
            ),
        )
    }

    @Test
    fun `toScheduledMedicationItems uses active date window and generic display name`() {
        val medicamento = medicamento(
            nome = "MEDICAMENTO GENERICO",
            compostoAtivo = "Dipirona Sodica",
            frequency = frequenciaHorariosEspecificos(
                horarios = listOf(LocalTime.of(8, 0)),
                dataInicio = LocalDate.parse("2026-05-25"),
                dataTermino = LocalDate.parse("2026-05-26"),
                usoContinuo = false,
            ),
        )

        val items = medicamento.toScheduledMedicationItems(
            datesToShow = getDatesBetween(
                LocalDate.parse("2026-05-24"),
                LocalDate.parse("2026-05-27"),
            ),
            now = LocalDateTime.parse("2026-05-25T07:00:00"),
        )

        assertEquals(2, items.size)
        assertEquals(
            listOf(LocalDate.parse("2026-05-25"), LocalDate.parse("2026-05-26")),
            items.map {
                it.date
            },
        )
        assertTrue(items.all { it.item.isGenerico })
        assertTrue(items.all { it.item.nomeExibicao == "Dipirona Sodica" })
    }

    @Test
    fun `organizeMedicationsByDay sorts medication items by time`() {
        val early = medicamento(
            id = 1,
            nome = "Atenolol",
            frequency = frequenciaHorariosEspecificos(listOf(LocalTime.of(8, 0))),
        )
        val late = medicamento(
            id = 2,
            nome = "Sinvastatina",
            frequency = frequenciaHorariosEspecificos(listOf(LocalTime.of(22, 0))),
        )

        val result = organizeMedicationsByDay(
            medicamentos = listOf(late, early),
            currentDate = LocalDate.parse("2026-05-25"),
            maxDaysToShow = 1,
            now = LocalDateTime.parse("2026-05-25T07:00:00"),
        )

        assertEquals(
            listOf("08:00", "22:00"),
            result.getValue(LocalDate.parse("2026-05-25")).map {
                it.horario
            },
        )
    }

    private fun medicamento(
        id: Long = 1,
        nome: String = "Losartana",
        compostoAtivo: String = "Losartana Potassica",
        frequency: FrequenciaUsoDomain = frequenciaHorariosEspecificos(listOf(LocalTime.of(8, 0))),
    ) = MedicamentoDomain(
        id = id,
        nome = nome,
        compostoAtivo = compostoAtivo,
        dosagem = "50mg",
        imagemUrl = null,
        frequenciaUso = frequency,
    )

    private fun frequenciaHorariosEspecificos(
        horarios: List<LocalTime>,
        dataInicio: LocalDate? = null,
        dataTermino: LocalDate? = null,
        usoContinuo: Boolean = true,
    ) = FrequenciaUsoDomain(
        frequenciaUsoTipo = FrequenciaUsoTipo.HORARIOS_ESPECIFICOS,
        usoContinuo = usoContinuo,
        horariosEspecificos = horarios,
        intervaloHoras = null,
        primeiroHorario = null,
        dataInicio = dataInicio,
        dataTermino = dataTermino,
    )
}
