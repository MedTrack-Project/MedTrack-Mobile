package com.medtrack.mobile.domain.model

data class MedicamentoCapturadoDomain(
    val nome: String,
    val compostoAtivo: String,
    val dosagem: String,
    val quantidade: String,
    val validade: String?,
)
