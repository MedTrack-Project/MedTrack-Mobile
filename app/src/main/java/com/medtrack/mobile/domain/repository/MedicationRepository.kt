package com.medtrack.mobile.domain.repository

import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.LoginResult
import com.medtrack.mobile.domain.model.MedicamentoDomain

interface MedicationRepository {
    suspend fun synchronizeUserData(token: String): LoginResult
    suspend fun findMedication(medicamentoId: Long): MedicamentoDomain?
    suspend fun confirmedDoseKeys(): Set<String>
    suspend fun confirmMedication(command: ConfirmationCommand)
}
