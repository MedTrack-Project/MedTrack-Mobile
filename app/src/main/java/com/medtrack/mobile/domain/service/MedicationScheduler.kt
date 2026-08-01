package com.medtrack.mobile.domain.service

import com.medtrack.mobile.domain.model.MedicamentoDomain

interface MedicationScheduler {
    suspend fun schedule(medicamento: MedicamentoDomain)
}
