package com.medtrack.mobile.domain.model.mappers

import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.domain.model.ConfirmacaoDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfirmacaoMappersTest {

    @Test
    fun `confirmacao entity maps to domain and back without losing sync state`() {
        val entity = ConfirmacaoEntity(
            id = 7,
            medicamentoId = 42,
            horario = "08:00",
            data = "2026-05-25",
            foiTomado = true,
            observacao = "Dose confirmada pelo app",
            sincronizado = true,
        )

        val domain = entity.toDomain()
        val mappedBack = domain.toEntity()

        assertEquals(
            ConfirmacaoDomain(
                id = 7,
                medicamentoId = 42,
                horario = "08:00",
                data = "2026-05-25",
                foiTomado = true,
                observacao = "Dose confirmada pelo app",
                sincronizado = true,
            ),
            domain,
        )
        assertEquals(entity, mappedBack)
    }
}
