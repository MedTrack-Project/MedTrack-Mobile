package com.medtrack.mobile.ui.screen.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.repository.ScanRepository
import com.medtrack.mobile.domain.usecase.QueueOfflineScanUseCase
import com.medtrack.mobile.domain.usecase.ScanMedicationUseCase
import com.medtrack.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `offline capture request displays confirmation dialog`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(CameraIntent.RequestCapture(isOnline = false))
        assertTrue(viewModel.uiState.value.showOfflineDialog)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `capture failure exposes stable error state`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(CameraIntent.CaptureFailed)
        assertEquals("Nao foi possivel capturar a imagem.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `selected dose is restored without persisting medication payload`() = runTest {
        val handle = SavedStateHandle()
        val first = viewModel(handle)
        first.onIntent(CameraIntent.SelectDose(SelectedDose(9, "2026-08-01", "08:30:00")))
        val restored = viewModel(handle)
        assertEquals(SelectedDose(9, "2026-08-01", "08:30"), restored.uiState.value.selectedDose)
        assertEquals(null, restored.uiState.value.medicamento)
    }

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()): CameraViewModel {
        val repository = CameraScanRepository()
        return CameraViewModel(
            ScanMedicationUseCase(repository),
            QueueOfflineScanUseCase(repository),
            handle,
        )
    }
}

private class CameraScanRepository : ScanRepository {
    override suspend fun scan(image: ImageReference): MedicamentoCapturadoDomain? = null
    override suspend fun enqueue(image: ImageReference) = Unit
}
