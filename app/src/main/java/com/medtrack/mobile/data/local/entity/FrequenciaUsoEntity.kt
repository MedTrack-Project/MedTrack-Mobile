package com.medtrack.mobile.data.local.entity

data class FrequenciaUsoEntity(
    val frequenciaUsoTipo: String,
    val usoContinuo: Boolean,
    val horariosEspecificos: String, // JSON string — precisa de TypeConverter
    val intervaloHoras: Int?,
    val primeiroHorario: String?, // "HH:mm"
    val dataInicio: String?, // "yyyy-MM-dd"
    val dataTermino: String?,
)
