package com.medtrack.mobile.domain.model

data class PendingScan(val id: Int, val image: ImageReference, val idempotencyKey: String, val attemptCount: Int)

data class ProcessedScan(val pendingScan: PendingScan, val medicamento: MedicamentoCapturadoDomain)
