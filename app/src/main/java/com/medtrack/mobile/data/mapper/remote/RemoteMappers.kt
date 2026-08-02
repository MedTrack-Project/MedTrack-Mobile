package com.medtrack.mobile.data.mapper.remote

import com.medtrack.mobile.data.remote.dto.FrequenciaUsoDto
import com.medtrack.mobile.data.remote.dto.MedicamentoDto
import com.medtrack.mobile.data.remote.dto.MedicamentoScanDto
import com.medtrack.mobile.data.remote.dto.UsuarioDto
import com.medtrack.mobile.domain.model.FrequenciaUsoDomain
import com.medtrack.mobile.domain.model.FrequenciaUsoTipo
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario
import java.time.LocalDate
import java.time.LocalTime

fun UsuarioDto.toDomain() = Usuario(
    id = id,
    nome = nome,
    email = email,
    nomeUsuario = nomeUsuario,
)

fun MedicamentoDto.toDomain() = MedicamentoDomain(
    id = id,
    nome = nome,
    compostoAtivo = compostoAtivo,
    dosagem = dosagem,
    imagemUrl = imagemUrl,
    frequenciaUso = frequenciaUso.toDomain(),
)

fun FrequenciaUsoDto.toDomain() = FrequenciaUsoDomain(
    frequenciaUsoTipo = FrequenciaUsoTipo.valueOf(frequenciaUsoTipo),
    usoContinuo = usoContinuo,
    horariosEspecificos = horariosEspecificos.map { LocalTime.parse(it) },
    intervaloHoras = intervaloHoras,
    primeiroHorario = primeiroHorario?.let { LocalTime.parse(it) },
    dataInicio = dataInicio?.let { LocalDate.parse(it) },
    dataTermino = dataTermino?.let { LocalDate.parse(it) },
)

fun MedicamentoScanDto.toCapturadoDomain() = MedicamentoCapturadoDomain(
    nome = nome ?: "Nao identificado",
    compostoAtivo = agenteAtivo ?: "Nao identificado",
    dosagem = dosagem ?: "N/A",
    quantidade = quantidade ?: "0",
    validade = validade ?: "",
)
