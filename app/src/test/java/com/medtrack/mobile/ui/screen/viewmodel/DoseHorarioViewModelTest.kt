package com.medtrack.mobile.ui.screen.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medtrack.mobile.data.repository.LoginData
import com.medtrack.mobile.data.repository.MedicamentoRepositoryContract
import com.medtrack.mobile.domain.model.DoseStatus
import com.medtrack.mobile.domain.model.FrequenciaUsoDomain
import com.medtrack.mobile.domain.model.FrequenciaUsoTipo
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.usecase.doseKey
import com.medtrack.mobile.testing.MainDispatcherRule
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DoseHorarioViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `carregarDose emits success when medication exists`() = runTest {
        val medicamento = medicamento()
        val repository = FakeMedicamentoRepository(
            medicamentoLocal = medicamento,
            dosesConfirmadas = setOf(
                doseKey(medicamento.id, LocalDate.parse("2026-05-25"), "08:00"),
            ),
        )
        val viewModel = DoseHorarioViewModel(repository)

        viewModel.carregarDose(
            medicamentoId = medicamento.id,
            data = "2026-05-25",
            horario = "08:00",
        )

        val state = viewModel.uiState.value
        assertTrue(state is DoseHorarioUiState.Success)
        state as DoseHorarioUiState.Success
        assertEquals(medicamento, state.medicamento)
        assertEquals(DoseStatus.CONFIRMED, state.status)
    }

    @Test
    fun `carregarDose emits error when medication is not found`() = runTest {
        val viewModel = DoseHorarioViewModel(FakeMedicamentoRepository(medicamentoLocal = null))

        viewModel.carregarDose(
            medicamentoId = 999,
            data = "2026-05-25",
            horario = "08:00",
        )

        assertEquals(
            DoseHorarioUiState.Error("Medicamento nao encontrado."),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `carregarDose emits repository error message when loading fails`() = runTest {
        val viewModel = DoseHorarioViewModel(
            FakeMedicamentoRepository(error = RuntimeException("falha ao carregar local")),
        )

        viewModel.carregarDose(
            medicamentoId = 1,
            data = "2026-05-25",
            horario = "08:00",
        )

        assertEquals(
            DoseHorarioUiState.Error("falha ao carregar local"),
            viewModel.uiState.value,
        )
    }

    private fun medicamento() = MedicamentoDomain(
        id = 1,
        nome = "Losartana",
        compostoAtivo = "Losartana Potassica",
        dosagem = "50mg",
        imagemUrl = null,
        frequenciaUso = FrequenciaUsoDomain(
            frequenciaUsoTipo = FrequenciaUsoTipo.HORARIOS_ESPECIFICOS,
            usoContinuo = true,
            horariosEspecificos = listOf(LocalTime.of(8, 0)),
            intervaloHoras = null,
            primeiroHorario = null,
            dataInicio = LocalDate.parse("2026-05-01"),
            dataTermino = null,
        ),
    )
}

private class FakeMedicamentoRepository(
    private val medicamentoLocal: MedicamentoDomain? = null,
    private val dosesConfirmadas: Set<String> = emptySet(),
    private val error: Exception? = null,
) : MedicamentoRepositoryContract {

    override suspend fun sincronizarDadosDoUsuario(token: String): LoginData {
        error("Nao usado neste teste")
    }

    override suspend fun buscarMedicamentoLocal(medicamentoId: Long): MedicamentoDomain? {
        error?.let { throw it }
        return medicamentoLocal
    }

    override suspend fun buscarChavesDeDosesConfirmadas(): Set<String> = dosesConfirmadas

    override suspend fun confirmarMedicamento(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        medicamentoSelecionadoId: Long?,
        dataSelecionada: String?,
        horarioSelecionado: String?,
    ) {
        error("Nao usado neste teste")
    }
}
