package com.example.piec_1.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.piec_1.data.local.entity.NotificacaoEntity

@Dao
interface NotificacaoDao {
    @Insert
    suspend fun insert(notificacao: NotificacaoEntity): Long

    @Query("SELECT * FROM notificacoes WHERE medicamentoId = :medicamentoId AND exibida = 0")
    suspend fun getNotificacoesPendentes(medicamentoId: Long): List<NotificacaoEntity>

    @Query("DELETE FROM notificacoes WHERE medicamentoId = :medicamentoId")
    suspend fun deleteByMedicamentoId(medicamentoId: Long)

    @Update
    suspend fun marcarComoExibida(notificacao: NotificacaoEntity)
}
