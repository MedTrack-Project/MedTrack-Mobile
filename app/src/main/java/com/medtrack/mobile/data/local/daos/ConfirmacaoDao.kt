package com.medtrack.mobile.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity

@Dao
interface ConfirmacaoDao {
    @Insert
    suspend fun insert(confirmacao: ConfirmacaoEntity): Long

    @Query(
        "SELECT * FROM confirmacoes WHERE medicamentoId = :medicamentoId AND data = :data AND horario = :horario",
    )
    suspend fun getConfirmacao(medicamentoId: Long, data: String, horario: String): ConfirmacaoEntity?

    @Query("SELECT * FROM confirmacoes WHERE sincronizado = 0")
    suspend fun getConfirmacoesNaoSincronizadas(): List<ConfirmacaoEntity>

    @Query("SELECT * FROM confirmacoes")
    suspend fun getAll(): List<ConfirmacaoEntity>

    @Update
    suspend fun update(confirmacao: ConfirmacaoEntity)

    @Query("DELETE FROM confirmacoes")
    suspend fun deleteAll()
}
