package com.example.piec_1.domain.model

data class MedicamentoCapturadoDomain(
    val nome: String,
    val compostoAtivo: String,
    val dosagem: String,
    val quantidade: String,
    val validade: String?,
)
