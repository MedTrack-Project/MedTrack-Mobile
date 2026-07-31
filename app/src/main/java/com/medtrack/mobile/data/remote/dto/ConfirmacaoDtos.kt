package com.medtrack.mobile.data.remote.dto

data class ConfirmacaoRequestDto(
    val usuarioId: Long,
    val medicamentoId: Long,
    val horario: String,
    val data: String,
    val foiTomado: Boolean,
    val observacao: String?,
)

data class ConfirmacaoResponseDto(
    val id: Long,
    val medicamentoId: Long,
    val usuarioId: Long,
    val horario: String,
    val data: String,
    val foiTomado: Boolean,
    val observacao: String?,
    val comprovanteImagemUrl: String? = null,
    val mensagem: String? = null,
)
