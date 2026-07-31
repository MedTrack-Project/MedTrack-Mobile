package com.medtrack.mobile.domain.model

data class ConfirmacaoDomain(
    val id: Long = 0,
    val medicamentoId: Long,
    val horario: String,
    val data: String,
    val foiTomado: Boolean,
    val observacao: String? = null,
    val sincronizado: Boolean = false,
)
