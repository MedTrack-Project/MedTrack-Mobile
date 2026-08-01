package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.model.LoginResult
import com.medtrack.mobile.domain.repository.AuthenticationRepository
import com.medtrack.mobile.domain.repository.MedicationRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val medicationRepository: MedicationRepository,
) {
    suspend operator fun invoke(username: String, password: String): LoginResult {
        val token = authenticationRepository.login(username, password)
        return medicationRepository.synchronizeUserData(token)
    }
}

class GetConfirmedDosesUseCase @Inject constructor(private val medicationRepository: MedicationRepository) {
    suspend operator fun invoke(): Set<String> = medicationRepository.confirmedDoseKeys()
}
