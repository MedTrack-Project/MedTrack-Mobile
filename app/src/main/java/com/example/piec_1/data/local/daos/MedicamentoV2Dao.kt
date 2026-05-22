package com.example.piec_1.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.piec_1.data.local.entity.MedicamentoEntity

@Dao
interface MedicamentoV2Dao {
    @Query("SELECT * FROM medicamentos_v2")
    suspend fun getAll(): List<MedicamentoEntity>

    @Query("SELECT * FROM medicamentos_v2 WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MedicamentoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicamentos: List<MedicamentoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicamento: MedicamentoEntity)

    @Query("DELETE FROM medicamentos_v2")
    suspend fun deleteAll()
}
