package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.LoginResult
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario
import com.medtrack.mobile.domain.repository.AuthenticationRepository
import com.medtrack.mobile.domain.repository.MedicationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginUseCaseTest {
    @Test
    fun `login authenticates and synchronizes user data`() = runTest {
        val authentication = FakeAuthenticationRepository("token-123")
        val expected = LoginResult(
            token = "token-123",
            usuario = Usuario(7, "Yann", "yann@example.test", "yann"),
            medicamentos = emptyList(),
        )
        val medication = FakeMedicationRepository(expected)

        val result = LoginUseCase(authentication, medication)("user", "password")

        assertEquals(expected, result)
        assertEquals("token-123", medication.synchronizedToken)
    }
}

private class FakeAuthenticationRepository(private val token: String) : AuthenticationRepository {
    override suspend fun login(username: String, password: String): String = token
}

private class FakeMedicationRepository(private val result: LoginResult) : MedicationRepository {
    var synchronizedToken: String? = null

    override suspend fun synchronizeUserData(token: String): LoginResult {
        synchronizedToken = token
        return result
    }

    override suspend fun findMedication(medicamentoId: Long): MedicamentoDomain? = null
    override suspend fun confirmedDoseKeys(): Set<String> = emptySet()
    override suspend fun confirmMedication(command: ConfirmationCommand) = Unit
}
