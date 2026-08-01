package com.medtrack.mobile.data.mapper.local

import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.domain.model.ConfirmacaoDomain

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
