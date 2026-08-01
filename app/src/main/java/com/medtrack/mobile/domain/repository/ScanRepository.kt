package com.medtrack.mobile.domain.repository

import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain

interface ScanRepository {
    suspend fun scan(image: ImageReference): MedicamentoCapturadoDomain?
    suspend fun enqueue(image: ImageReference)
}
