package com.example.piec_1.domain.model.mappers

import com.example.piec_1.data.local.entity.ConfirmacaoEntity
import com.example.piec_1.domain.model.ConfirmacaoDomain

fun ConfirmacaoEntity.toDomain() = ConfirmacaoDomain(
    id = id,
    medicamentoId = medicamentoId,
    horario = horario,
    data = data,
    foiTomado = foiTomado,
    observacao = observacao,
    sincronizado = sincronizado,
)

fun ConfirmacaoDomain.toEntity() = ConfirmacaoEntity(
    id = id,
    medicamentoId = medicamentoId,
    horario = horario,
    data = data,
    foiTomado = foiTomado,
    observacao = observacao,
    sincronizado = sincronizado,
)
