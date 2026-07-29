package com.example.piec_1.data.remote.mapper

import com.example.piec_1.data.remote.dto.FrequenciaUsoDto
import com.example.piec_1.data.remote.dto.MedicamentoScanDto
import com.example.piec_1.data.remote.dto.UsuarioDto
import com.example.piec_1.domain.model.FrequenciaUsoTipo
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteMappersTest {

    @Test
    fun `usuario dto maps to domain`() {
        val domain = UsuarioDto(
            id = 3,
            nome = "Maria Silva",
            email = "maria@example.com",
            nomeUsuario = "maria",
        ).toDomain()

        assertEquals(3L, domain.id)
        assertEquals("Maria Silva", domain.nome)
        assertEquals("maria@example.com", domain.email)
        assertEquals("maria", domain.nomeUsuario)
    }

    @Test
    fun `scan dto maps captured medication values`() {
        val domain = MedicamentoScanDto(
            nome = "Losartana",
            agente_ativo = "Losartana Potassica",
            dosagem = "50mg",
            quantidade = "30 comprimidos",
            validade = "2027-01",
        ).toCapturadoDomain()

        assertEquals("Losartana", domain.nome)
        assertEquals("Losartana Potassica", domain.compostoAtivo)
        assertEquals("50mg", domain.dosagem)
        assertEquals("30 comprimidos", domain.quantidade)
        assertEquals("2027-01", domain.validade)
    }

    @Test
    fun `scan dto uses fallback values when OCR response is incomplete`() {
        val domain = MedicamentoScanDto(
            nome = null,
            agente_ativo = null,
            dosagem = null,
            quantidade = null,
            validade = null,
        ).toCapturadoDomain()

        assertEquals("Nao identificado", domain.nome)
        assertEquals("Nao identificado", domain.compostoAtivo)
        assertEquals("N/A", domain.dosagem)
        assertEquals("0", domain.quantidade)
        assertEquals("", domain.validade)
    }

    @Test
    fun `frequencia dto maps nullable optional fields`() {
        val domain = FrequenciaUsoDto(
            frequenciaUsoTipo = "INTERVALO_ENTRE_DOSES",
            usoContinuo = false,
            horariosEspecificos = emptyList(),
            intervaloHoras = 8,
            primeiroHorario = null,
            dataInicio = null,
            dataTermino = null,
        ).toDomain()

        assertEquals(FrequenciaUsoTipo.INTERVALO_ENTRE_DOSES, domain.frequenciaUsoTipo)
        assertEquals(false, domain.usoContinuo)
        assertEquals(emptyList<LocalTime>(), domain.horariosEspecificos)
        assertEquals(8, domain.intervaloHoras)
        assertNull(domain.primeiroHorario)
        assertNull(domain.dataInicio)
        assertNull(domain.dataTermino)
    }

    @Test
    fun `scan dto applies fallbacks only to missing fields`() {
        val domain = MedicamentoScanDto(
            nome = "Atenolol",
            agente_ativo = null,
            dosagem = "25mg",
            quantidade = null,
            validade = "2028-02",
        ).toCapturadoDomain()

        assertEquals("Atenolol", domain.nome)
        assertEquals("Nao identificado", domain.compostoAtivo)
        assertEquals("25mg", domain.dosagem)
        assertEquals("0", domain.quantidade)
        assertEquals("2028-02", domain.validade)
    }
}
