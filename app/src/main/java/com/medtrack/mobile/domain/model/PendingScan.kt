package com.medtrack.mobile.domain.model

data class PendingScan(val id: Int, val image: ImageReference)

data class ProcessedScan(val pendingScan: PendingScan, val medicamento: MedicamentoCapturadoDomain)
