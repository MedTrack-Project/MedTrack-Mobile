package com.example.piec_1.domain.model.mappers

import com.example.piec_1.data.local.entity.FrequenciaUsoEntity
import com.example.piec_1.data.local.entity.MedicamentoEntity
import com.example.piec_1.domain.model.FrequenciaUsoDomain
import com.example.piec_1.domain.model.FrequenciaUsoTipo
import com.example.piec_1.domain.model.MedicamentoDomain
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime

fun FrequenciaUsoEntity.toDomain() = FrequenciaUsoDomain(
    frequenciaUsoTipo = FrequenciaUsoTipo.valueOf(frequenciaUsoTipo),
    usoContinuo = usoContinuo,
    horariosEspecificos = Gson()
        .fromJson<List<String>>(horariosEspecificos, object : TypeToken<List<String>>() {}.type)
        .map { LocalTime.parse(it) },
    intervaloHoras = intervaloHoras,
    primeiroHorario = primeiroHorario?.let { LocalTime.parse(it) },
    dataInicio = dataInicio?.let { LocalDate.parse(it) },
    dataTermino = dataTermino?.let { LocalDate.parse(it) }
)

fun FrequenciaUsoDomain.toEntity() = FrequenciaUsoEntity(
    frequenciaUsoTipo = frequenciaUsoTipo.name,
    usoContinuo = usoContinuo,
    horariosEspecificos = Gson().toJson(horariosEspecificos.map { it.toString() }),
    intervaloHoras = intervaloHoras,
    primeiroHorario = primeiroHorario?.toString(),
    dataInicio = dataInicio?.toString(),
    dataTermino = dataTermino?.toString()
)

fun MedicamentoEntity.toDomain() = MedicamentoDomain(
    id = id,
    nome = nome,
    compostoAtivo = compostoAtivo,
    dosagem = dosagem,
    imagemUrl = imagemUrl,
    frequenciaUso = frequenciaUso.toDomain()
)

fun MedicamentoDomain.toEntity() = MedicamentoEntity(
    id = id,
    nome = nome,
    compostoAtivo = compostoAtivo,
    dosagem = dosagem,
    imagemUrl = imagemUrl,
    frequenciaUso = frequenciaUso.toEntity()
)
