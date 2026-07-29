package com.example.piec_1.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos_v2")
data class MedicamentoEntity(
    @PrimaryKey val id: Long,
    val nome: String,
    val compostoAtivo: String,
    val dosagem: String,
    val imagemUrl: String?,
    @Embedded(prefix = "freq_") val frequenciaUso: FrequenciaUsoEntity,
)
