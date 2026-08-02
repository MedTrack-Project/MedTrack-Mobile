package com.medtrack.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScanResponseDto(val status: String, val data: MedicamentoScanDto?, val count: Int)

data class MedicamentoScanDto(
    val nome: String?,
    @SerializedName("agente_ativo") val agenteAtivo: String?,
    val dosagem: String?,
    val quantidade: String?,
    val validade: String? = null,
)
