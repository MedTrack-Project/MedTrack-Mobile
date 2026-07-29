package com.example.piec_1.data.remote.dto

data class ScanResponseDto(val status: String, val data: MedicamentoScanDto?, val count: Int)

data class MedicamentoScanDto(
    val nome: String?,
    val agente_ativo: String?,
    val dosagem: String?,
    val quantidade: String?,
    val validade: String? = null,
)
