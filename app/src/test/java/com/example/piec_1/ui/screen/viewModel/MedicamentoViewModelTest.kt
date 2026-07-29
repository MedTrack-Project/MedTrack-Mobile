package com.example.piec_1.ui.screen.viewModel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.piec_1.data.repository.LoginData
import com.example.piec_1.data.repository.MedicamentoRepositoryContract
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.testing.MainDispatcherRule
import com.example.piec_1.utils.exceptions.ConfirmacaoExistenteException
import com.example.piec_1.utils.exceptions.DoseForaDoHorarioException
import com.example.piec_1.utils.exceptions.MedicamentoNaoEncontradoException
import com.example.piec_1.utils.exceptions.TokenNaoEncontradoException
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
    fun `confirmarMedicamento emits success and invokes success callback`() = runTest {
        var successCalled = false
        var errorMessage: String? = null
        val viewModel = MedicamentoViewModel(ConfirmacaoFakeDataSource())

        viewModel.confirmarMedicamento(
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
    fun `confirmarMedicamento emits translated error and invokes error callback`() = runTest {
        var successCalled = false
        var errorMessage: String? = null
        val viewModel = MedicamentoViewModel(
            ConfirmacaoFakeDataSource(error = RuntimeException("falha controlada")),
        )

        viewModel.confirmarMedicamento(
            medicamentoCapturado = medicamentoCapturado(),
            comprovanteImagemUri = null,
            selectedDose = SelectedDose(medicamentoId = 1, data = "2026-05-25", horario = "08:00"),
            onSuccess = { successCalled = true },
            onError = { errorMessage = it },
        )

        val expectedMessage = "Erro ao confirmar: falha controlada"
        assertEquals(false, successCalled)
        assertEquals(expectedMessage, errorMessage)
        assertEquals(
            MedicamentoViewModel.MedicamentoUIState.Error(expectedMessage),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `confirmarMedicamento translates domain exceptions to user messages`() = runTest {
        val cases = listOf(
            TokenNaoEncontradoException() to "Sessao expirada. Faca login novamente.",
            MedicamentoNaoEncontradoException() to
                "Medicamento nao cadastrado. Cadastre-o primeiro.",
            ConfirmacaoExistenteException() to "Ja existe uma confirmacao para este horario.",
            DoseForaDoHorarioException() to "Esta dose so pode ser confirmada no horario correto.",
        )

        cases.forEach { (exception, expectedMessage) ->
            var successCalled = false
            var errorMessage: String? = null
            val viewModel = MedicamentoViewModel(ConfirmacaoFakeDataSource(error = exception))

            viewModel.confirmarMedicamento(
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

private class ConfirmacaoFakeDataSource(private val error: Exception? = null) : MedicamentoRepositoryContract {

    override suspend fun sincronizarDadosDoUsuario(token: String): LoginData =
        throw AssertionError("Nao usado neste teste")

    override suspend fun buscarMedicamentoLocal(medicamentoId: Long): MedicamentoDomain? =
        throw AssertionError("Nao usado neste teste")

    override suspend fun buscarChavesDeDosesConfirmadas(): Set<String> = throw AssertionError("Nao usado neste teste")

    override suspend fun confirmarMedicamento(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        medicamentoSelecionadoId: Long?,
        dataSelecionada: String?,
        horarioSelecionado: String?,
    ) {
        error?.let { throw it }
    }
}
