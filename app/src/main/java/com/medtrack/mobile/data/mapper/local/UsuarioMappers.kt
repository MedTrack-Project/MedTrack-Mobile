package com.medtrack.mobile.data.mapper.local

import com.medtrack.mobile.data.local.entity.UsuarioEntity
import com.medtrack.mobile.domain.model.Usuario

fun Usuario.toEntity() = UsuarioEntity(
    id = id,
    nome = nome,
    email = email,
    nomeUsuario = nomeUsuario,
)

fun UsuarioEntity.toDomain() = Usuario(
    id = id,
    nome = nome,
    email = email,
    nomeUsuario = nomeUsuario,
)
