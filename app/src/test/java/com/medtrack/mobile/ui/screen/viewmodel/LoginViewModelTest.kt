package com.medtrack.mobile.ui.screen.viewmodel

import com.medtrack.mobile.domain.error.InvalidCredentialsException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.LoginResult
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario
import com.medtrack.mobile.domain.repository.AuthenticationRepository
import com.medtrack.mobile.domain.repository.MedicationRepository
import com.medtrack.mobile.domain.usecase.GetConfirmedDosesUseCase
import com.medtrack.mobile.domain.usecase.LoginUseCase
import com.medtrack.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submit exposes authenticated content`() = runTest {
        val medication = LoginMedicationRepository()
        val viewModel = LoginViewModel(
            LoginUseCase(LoginAuthenticationRepository(), medication),
            GetConfirmedDosesUseCase(medication),
        )

        viewModel.onIntent(LoginIntent.Submit("yann", "secret"))

        assertEquals("Yann", viewModel.uiState.value.usuario?.nome)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `invalid credentials expose friendly error`() = runTest {
        val medication = LoginMedicationRepository()
        val viewModel = LoginViewModel(
            LoginUseCase(LoginAuthenticationRepository(InvalidCredentialsException()), medication),
            GetConfirmedDosesUseCase(medication),
        )

        viewModel.onIntent(LoginIntent.Submit("invalid", "invalid"))

        assertEquals("Usuario ou senha invalidos", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}

private class LoginAuthenticationRepository(private val failure: Exception? = null) : AuthenticationRepository {
    override suspend fun login(username: String, password: String): String {
        failure?.let { throw it }
        return "token"
    }
}

private class LoginMedicationRepository : MedicationRepository {
    override suspend fun synchronizeUserData(token: String) = LoginResult(
        token,
        Usuario(1, "Yann", "yann@example.test", "yann"),
        emptyList(),
    )
    override suspend fun findMedication(medicamentoId: Long): MedicamentoDomain? = null
    override suspend fun confirmedDoseKeys(): Set<String> = setOf("1|2026-08-01|08:00")
    override suspend fun confirmMedication(command: ConfirmationCommand) = Unit
}
