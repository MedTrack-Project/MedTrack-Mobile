package com.medtrack.mobile.data.remote.dto

data class MedicamentoDto(
    val id: Long,
    val nome: String,
    val compostoAtivo: String,
    val dosagem: String,
    val imagemUrl: String? = null,
    val frequenciaUso: FrequenciaUsoDto,
)

data class FrequenciaUsoDto(
    val frequenciaUsoTipo: String,
    val usoContinuo: Boolean,
    val horariosEspecificos: List<String> = emptyList(),
    val intervaloHoras: Int? = null,
    val primeiroHorario: String? = null,
    val dataInicio: String? = null,
    val dataTermino: String? = null,
)
