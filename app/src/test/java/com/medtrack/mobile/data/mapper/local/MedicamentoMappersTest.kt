package com.medtrack.mobile.data.mapper.local

import com.medtrack.mobile.data.local.entity.FrequenciaUsoEntity
import com.medtrack.mobile.data.local.entity.MedicamentoEntity
import com.medtrack.mobile.data.mapper.remote.toDomain
import com.medtrack.mobile.data.remote.dto.FrequenciaUsoDto
import com.medtrack.mobile.data.remote.dto.MedicamentoDto
import com.medtrack.mobile.domain.model.FrequenciaUsoDomain
import com.medtrack.mobile.domain.model.FrequenciaUsoTipo
import com.medtrack.mobile.domain.model.MedicamentoDomain
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicamentoMappersTest {

    @Test
    fun `medicamento dto to domain preserves medication and frequency fields`() {
        val dto = MedicamentoDto(
            id = 10,
            nome = "Losartana",
            compostoAtivo = "Losartana Potassica",
            dosagem = "50mg",
            imagemUrl = "https://example.com/losartana.png",
            frequenciaUso = FrequenciaUsoDto(
                frequenciaUsoTipo = "HORARIOS_ESPECIFICOS",
                usoContinuo = true,
                horariosEspecificos = listOf("08:00", "20:00"),
                intervaloHoras = null,
                primeiroHorario = "08:00",
                dataInicio = "2026-05-20",
                dataTermino = null,
            ),
        )

        val domain = dto.toDomain()

        assertEquals(10L, domain.id)
        assertEquals("Losartana", domain.nome)
        assertEquals("Losartana Potassica", domain.compostoAtivo)
        assertEquals("50mg", domain.dosagem)
        assertEquals("https://example.com/losartana.png", domain.imagemUrl)
        assertEquals(FrequenciaUsoTipo.HORARIOS_ESPECIFICOS, domain.frequenciaUso.frequenciaUsoTipo)
        assertEquals(
            listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
            domain.frequenciaUso.horariosEspecificos,
        )
        assertEquals(LocalDate.of(2026, 5, 20), domain.frequenciaUso.dataInicio)
        assertNull(domain.frequenciaUso.dataTermino)
    }

    @Test
    fun `medicamento entity maps to domain and back without losing values`() {
        val entity = MedicamentoEntity(
            id = 20,
            nome = "Metformina",
            compostoAtivo = "Cloridrato de Metformina",
            dosagem = "850mg",
            imagemUrl = null,
            frequenciaUso = FrequenciaUsoEntity(
                frequenciaUsoTipo = "INTERVALO_ENTRE_DOSES",
                usoContinuo = false,
                horariosEspecificos = """["06:30","18:30"]""",
                intervaloHoras = 12,
                primeiroHorario = "06:30",
                dataInicio = "2026-05-21",
                dataTermino = "2026-05-30",
            ),
        )

        val domain = entity.toDomain()
        val mappedBack = domain.toEntity()

        assertEquals(
            MedicamentoDomain(
                id = 20,
                nome = "Metformina",
                compostoAtivo = "Cloridrato de Metformina",
                dosagem = "850mg",
                imagemUrl = null,
                frequenciaUso = FrequenciaUsoDomain(
                    frequenciaUsoTipo = FrequenciaUsoTipo.INTERVALO_ENTRE_DOSES,
                    usoContinuo = false,
                    horariosEspecificos = listOf(LocalTime.of(6, 30), LocalTime.of(18, 30)),
                    intervaloHoras = 12,
                    primeiroHorario = LocalTime.of(6, 30),
                    dataInicio = LocalDate.of(2026, 5, 21),
                    dataTermino = LocalDate.of(2026, 5, 30),
                ),
            ),
            domain,
        )
        assertEquals(entity, mappedBack)
    }

    @Test
    fun `frequencia entity maps nullable optional fields`() {
        val entity = FrequenciaUsoEntity(
            frequenciaUsoTipo = "HORARIOS_ESPECIFICOS",
            usoContinuo = true,
            horariosEspecificos = "[]",
            intervaloHoras = null,
            primeiroHorario = null,
            dataInicio = null,
            dataTermino = null,
        )

        val domain = entity.toDomain()
        val mappedBack = domain.toEntity()

        assertEquals(FrequenciaUsoTipo.HORARIOS_ESPECIFICOS, domain.frequenciaUsoTipo)
        assertEquals(true, domain.usoContinuo)
        assertEquals(emptyList<LocalTime>(), domain.horariosEspecificos)
        assertNull(domain.intervaloHoras)
        assertNull(domain.primeiroHorario)
        assertNull(domain.dataInicio)
        assertNull(domain.dataTermino)
        assertEquals(entity, mappedBack)
    }
}
