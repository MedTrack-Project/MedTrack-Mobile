package com.medtrack.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(@PrimaryKey val id: Long, val nome: String, val email: String, val nomeUsuario: String)
