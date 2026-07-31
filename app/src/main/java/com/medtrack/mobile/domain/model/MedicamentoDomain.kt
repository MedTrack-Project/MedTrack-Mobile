package com.medtrack.mobile.domain.model

data class MedicamentoDomain(
    val id: Long,
    val nome: String,
    val compostoAtivo: String,
    val dosagem: String,
    val imagemUrl: String?,
    val frequenciaUso: FrequenciaUsoDomain,
)
