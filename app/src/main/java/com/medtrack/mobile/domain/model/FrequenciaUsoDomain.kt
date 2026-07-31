package com.medtrack.mobile.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class FrequenciaUsoDomain(
    val frequenciaUsoTipo: FrequenciaUsoTipo,
    val usoContinuo: Boolean,
    val horariosEspecificos: List<LocalTime>,
    val intervaloHoras: Int?,
    val primeiroHorario: LocalTime?,
    val dataInicio: LocalDate?,
    val dataTermino: LocalDate?,
)
