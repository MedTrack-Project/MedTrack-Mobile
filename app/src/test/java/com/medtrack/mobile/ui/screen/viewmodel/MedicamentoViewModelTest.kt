package com.medtrack.mobile.ui.screen.viewmodel

import com.medtrack.mobile.domain.error.ConfirmationAlreadyExistsException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.LoginResult
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.repository.MedicationRepository
import com.medtrack.mobile.domain.usecase.ConfirmMedicationUseCase
import com.medtrack.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicamentoViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `confirm transitions to success`() = runTest {
        val viewModel = MedicamentoViewModel(ConfirmMedicationUseCase(ConfirmationRepository()))
        viewModel.onIntent(MedicamentoIntent.Confirm(command()))
        assertEquals(MedicamentoUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `confirm maps unexpected error`() = runTest {
        val viewModel = MedicamentoViewModel(ConfirmMedicationUseCase(ConfirmationRepository(RuntimeException())))
        viewModel.onIntent(MedicamentoIntent.Confirm(command()))
        assertEquals(MedicamentoUiState.Error("Erro ao confirmar. Tente novamente."), viewModel.uiState.value)
    }

    @Test
    fun `confirm maps domain errors`() = runTest {
        val cases = listOf(
            InvalidSessionException() to "Sessao expirada. Faca login novamente.",
            ConfirmationAlreadyExistsException() to "Ja existe uma confirmacao para este horario.",
        )
        cases.forEach { (error, message) ->
            val viewModel = MedicamentoViewModel(ConfirmMedicationUseCase(ConfirmationRepository(error)))
            viewModel.onIntent(MedicamentoIntent.Confirm(command()))
            assertEquals(MedicamentoUiState.Error(message), viewModel.uiState.value)
        }
    }

    private fun command() = ConfirmationCommand(
        MedicamentoCapturadoDomain("Losartana", "Losartana Potassica", "50mg", "30", "2027-01"),
        null,
        1,
        "2026-05-25",
        "08:00",
    )
}

private class ConfirmationRepository(private val failure: Exception? = null) : MedicationRepository {
    override suspend fun synchronizeUserData(token: String): LoginResult = error("Nao usado")
    override suspend fun findMedication(medicamentoId: Long): MedicamentoDomain? = error("Nao usado")
    override suspend fun confirmedDoseKeys(): Set<String> = error("Nao usado")
    override suspend fun confirmMedication(command: ConfirmationCommand) {
        failure?.let { throw it }
    }
}
