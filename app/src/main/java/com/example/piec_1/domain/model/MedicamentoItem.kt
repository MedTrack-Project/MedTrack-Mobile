package com.example.piec_1.domain.model

import java.time.LocalDate
import java.util.UUID

data class MedicationItem(
    val id: String = UUID.randomUUID().toString(),
    val medicamentoId: Long,
    val date: LocalDate,
    val nomeExibicao: String,
    val horario: String,
    val dosagem: String,
    val imagemUrl: String?,
    val isContinuous: Boolean,
    val isGenerico: Boolean,
    val status: DoseStatus,
)

enum class DoseStatus {
    FUTURE,
    AVAILABLE,
    LATE,
    CONFIRMED,
    EXPIRED,
}
