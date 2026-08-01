package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.error.InvalidDoseException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.DoseStatus
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.repository.MedicationRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class ConfirmMedicationUseCase @Inject constructor(private val medicationRepository: MedicationRepository) {
    suspend operator fun invoke(command: ConfirmationCommand) = medicationRepository.confirmMedication(command)
}

class LoadDoseUseCase @Inject constructor(private val medicationRepository: MedicationRepository) {
    suspend operator fun invoke(medicamentoId: Long, data: String, horario: String): DoseDetails {
        val date = runCatching { LocalDate.parse(data) }.getOrElse { throw InvalidDoseException(it) }
        val time = runCatching { LocalTime.parse(horario) }.getOrElse { throw InvalidDoseException(it) }
        val medicamento = medicationRepository.findMedication(medicamentoId)
            ?: return DoseDetails.NotFound
        return DoseDetails.Found(
            medicamento = medicamento,
            status = resolveDoseStatus(
                medicamentoId = medicamentoId,
                date = date,
                horario = time,
                confirmedDoseKeys = medicationRepository.confirmedDoseKeys(),
            ),
        )
    }
}

sealed interface DoseDetails {
    data object NotFound : DoseDetails
    data class Found(val medicamento: MedicamentoDomain, val status: DoseStatus) : DoseDetails
}
