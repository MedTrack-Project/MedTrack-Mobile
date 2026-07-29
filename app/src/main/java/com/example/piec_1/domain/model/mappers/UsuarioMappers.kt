package com.example.piec_1.domain.model.mappers

import com.example.piec_1.data.local.entity.UsuarioEntity
import com.example.piec_1.domain.model.Usuario

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
