package com.example.piec_1.data.remote.mapper

import com.example.piec_1.data.remote.dto.FrequenciaUsoDto
import com.example.piec_1.data.remote.dto.MedicamentoDto
import com.example.piec_1.data.remote.dto.MedicamentoScanDto
import com.example.piec_1.data.remote.dto.UsuarioDto
import com.example.piec_1.domain.model.FrequenciaUsoDomain
import com.example.piec_1.domain.model.FrequenciaUsoTipo
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.domain.model.Usuario
import java.time.LocalDate
import java.time.LocalTime

fun UsuarioDto.toDomain() = Usuario(
    id = id,
    nome = nome,
    email = email,
    nomeUsuario = nomeUsuario
)

fun MedicamentoDto.toDomain() = MedicamentoDomain(
    id = id,
    nome = nome,
    compostoAtivo = compostoAtivo,
    dosagem = dosagem,
    imagemUrl = imagemUrl,
    frequenciaUso = frequenciaUso.toDomain()
)

fun FrequenciaUsoDto.toDomain() = FrequenciaUsoDomain(
    frequenciaUsoTipo = FrequenciaUsoTipo.valueOf(frequenciaUsoTipo),
    usoContinuo = usoContinuo,
    horariosEspecificos = horariosEspecificos.map { LocalTime.parse(it) },
    intervaloHoras = intervaloHoras,
    primeiroHorario = primeiroHorario?.let { LocalTime.parse(it) },
    dataInicio = dataInicio?.let { LocalDate.parse(it) },
    dataTermino = dataTermino?.let { LocalDate.parse(it) }
)

fun MedicamentoScanDto.toCapturadoDomain() = MedicamentoCapturadoDomain(
    nome = nome ?: "Nao identificado",
    compostoAtivo = agente_ativo ?: "Nao identificado",
    dosagem = dosagem ?: "N/A",
    quantidade = quantidade ?: "0",
    validade = validade ?: ""
)
