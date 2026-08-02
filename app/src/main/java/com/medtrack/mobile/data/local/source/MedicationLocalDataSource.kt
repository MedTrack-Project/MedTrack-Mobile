package com.medtrack.mobile.data.local.source

import androidx.room.withTransaction
import com.medtrack.mobile.data.local.AppDatabase
import com.medtrack.mobile.data.local.daos.ConfirmacaoDao
import com.medtrack.mobile.data.local.daos.MedicamentoV2Dao
import com.medtrack.mobile.data.local.daos.UsuarioDao
import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.data.local.entity.MedicamentoEntity
import com.medtrack.mobile.data.local.entity.UsuarioEntity
import javax.inject.Inject

interface MedicationLocalSource {
    suspend fun replaceUserSnapshot(user: UsuarioEntity, medicationItems: List<MedicamentoEntity>)
    suspend fun user(): UsuarioEntity
    suspend fun medication(id: Long): MedicamentoEntity?
    suspend fun medications(): List<MedicamentoEntity>
    suspend fun confirmations(): List<ConfirmacaoEntity>
    suspend fun confirmation(medicationId: Long, date: String, time: String): ConfirmacaoEntity?
    suspend fun saveConfirmation(value: ConfirmacaoEntity)
}

class MedicationLocalDataSource @Inject constructor(
    private val database: AppDatabase,
    private val users: UsuarioDao,
    private val medications: MedicamentoV2Dao,
    private val confirmations: ConfirmacaoDao,
) : MedicationLocalSource {
    override suspend fun replaceUserSnapshot(user: UsuarioEntity, medicationItems: List<MedicamentoEntity>) {
        database.withTransaction {
            users.insert(user)
            medications.deleteAll()
            medications.insertAll(medicationItems)
        }
    }

    override suspend fun user(): UsuarioEntity = users.getUsuario()
    override suspend fun medication(id: Long): MedicamentoEntity? = medications.getById(id)
    override suspend fun medications(): List<MedicamentoEntity> = medications.getAll()
    override suspend fun confirmations(): List<ConfirmacaoEntity> = confirmations.getAll()

    override suspend fun confirmation(medicationId: Long, date: String, time: String): ConfirmacaoEntity? =
        confirmations.getConfirmacao(medicationId, date, time)

    override suspend fun saveConfirmation(value: ConfirmacaoEntity) {
        database.withTransaction {
            val current = confirmations.getConfirmacao(value.medicamentoId, value.data, value.horario)
            if (current == null) confirmations.insert(value) else confirmations.update(value.copy(id = current.id))
        }
    }
}
