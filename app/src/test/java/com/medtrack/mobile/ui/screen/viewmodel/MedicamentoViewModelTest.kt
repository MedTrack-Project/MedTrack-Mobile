package com.medtrack.mobile.ui.screen.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medtrack.mobile.domain.error.ConfirmationAlreadyExistsException
import com.medtrack.mobile.domain.error.DoseOutsideAllowedTimeException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.MedicationNotFoundException
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicamentoViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `confirmMedication emits success and invokes success callback`() = runTest {
        var successCalled = false
        var errorMessage: String? = null
        val viewModel = MedicamentoViewModel(ConfirmMedicationUseCase(ConfirmacaoFakeDataSource()))

        viewModel.confirmMedication(
            medicamentoCapturado = medicamentoCapturado(),
            comprovanteImagemUri = null,
            selectedDose = SelectedDose(medicamentoId = 1, data = "2026-05-25", horario = "08:00"),
            onSuccess = { successCalled = true },
            onError = { errorMessage = it },
        )

        assertTrue(successCalled)
        assertEquals(null, errorMessage)
        assertEquals(
            MedicamentoViewModel.MedicamentoUIState.Success("Medicamento confirmado!"),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `confirmMedication emits translated error and invokes error callback`() = runTest {
        var successCalled = false
        var errorMessage: String? = null
        val viewModel = MedicamentoViewModel(
            ConfirmMedicationUseCase(ConfirmacaoFakeDataSource(error = RuntimeException("falha controlada"))),
        )

        viewModel.confirmMedication(
            medicamentoCapturado = medicamentoCapturado(),
            comprovanteImagemUri = null,
            selectedDose = SelectedDose(medicamentoId = 1, data = "2026-05-25", horario = "08:00"),
            onSuccess = { successCalled = true },
            onError = { errorMessage = it },
        )

        val expectedMessage = "Erro ao confirmar. Tente novamente."
        assertEquals(false, successCalled)
        assertEquals(expectedMessage, errorMessage)
        assertEquals(
            MedicamentoViewModel.MedicamentoUIState.Error(expectedMessage),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `confirmMedication translates domain exceptions to user messages`() = runTest {
        val cases = listOf(
            InvalidSessionException() to "Sessao expirada. Faca login novamente.",
            MedicationNotFoundException() to
                "Medicamento nao cadastrado. Cadastre-o primeiro.",
            ConfirmationAlreadyExistsException() to "Ja existe uma confirmacao para este horario.",
            DoseOutsideAllowedTimeException() to "Esta dose so pode ser confirmada no horario correto.",
        )

        cases.forEach { (exception, expectedMessage) ->
            var successCalled = false
            var errorMessage: String? = null
            val viewModel = MedicamentoViewModel(
                ConfirmMedicationUseCase(ConfirmacaoFakeDataSource(error = exception)),
            )

            viewModel.confirmMedication(
                medicamentoCapturado = medicamentoCapturado(),
                comprovanteImagemUri = null,
                selectedDose = SelectedDose(
                    medicamentoId = 1,
                    data = "2026-05-25",
                    horario = "08:00",
                ),
                onSuccess = { successCalled = true },
                onError = { errorMessage = it },
            )

            assertEquals(false, successCalled)
            assertEquals(expectedMessage, errorMessage)
            assertEquals(
                MedicamentoViewModel.MedicamentoUIState.Error(expectedMessage),
                viewModel.uiState.value,
            )
        }
    }

    private fun medicamentoCapturado() = MedicamentoCapturadoDomain(
        nome = "Losartana",
        compostoAtivo = "Losartana Potassica",
        dosagem = "50mg",
        quantidade = "30 comprimidos",
        validade = "2027-01",
    )
}

private class ConfirmacaoFakeDataSource(private val error: Exception? = null) : MedicationRepository {

    override suspend fun synchronizeUserData(token: String): LoginResult = throw AssertionError("Nao usado neste teste")

    override suspend fun findMedication(medicamentoId: Long): MedicamentoDomain? =
        throw AssertionError("Nao usado neste teste")

    override suspend fun confirmedDoseKeys(): Set<String> = throw AssertionError("Nao usado neste teste")

    override suspend fun confirmMedication(command: ConfirmationCommand) {
        error?.let { throw it }
    }
}
