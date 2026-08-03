package com.medtrack.mobile.ui.screen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.usecase.QueueOfflineScanUseCase
import com.medtrack.mobile.domain.usecase.ScanMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val scanMedication: ScanMedicationUseCase,
    private val queueOfflineScan: QueueOfflineScanUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState(selectedDose = savedDose()))
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CameraEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.RequestCapture -> requestCapture(intent.isOnline)
            is CameraIntent.PhotoCaptured -> processPhoto(intent.image, intent.offline)
            CameraIntent.ConfirmOfflineCapture -> confirmOfflineCapture()
            CameraIntent.CaptureFailed -> captureFailed()
            CameraIntent.DismissOfflineDialog -> _uiState.update { it.copy(showOfflineDialog = false) }
            is CameraIntent.SelectDose -> selectDose(intent.dose)
            is CameraIntent.UpdateMedication -> _uiState.update { it.copy(medicamento = intent.medicamento) }
        }
    }

    fun openMedicationFromNotification(medicamento: MedicamentoCapturadoDomain) {
        _uiState.update { it.copy(medicamento = medicamento) }
        _events.tryEmit(CameraEvent.NavigateToConfirmation)
    }

    private fun requestCapture(isOnline: Boolean) {
        if (!isOnline) {
            _uiState.update { it.copy(showOfflineDialog = true) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        _events.tryEmit(CameraEvent.CapturePhoto(offline = false))
    }

    private fun confirmOfflineCapture() {
        _uiState.update { it.copy(isLoading = true, showOfflineDialog = false, errorMessage = null) }
        _events.tryEmit(CameraEvent.CapturePhoto(offline = true))
    }

    private fun processPhoto(image: ImageReference, offline: Boolean) {
        _uiState.update { it.copy(isLoading = true, capturedPhoto = image, showOfflineDialog = false) }
        viewModelScope.launch {
            if (offline) queuePhoto(image) else scanPhoto(image)
        }
    }

    private suspend fun scanPhoto(image: ImageReference) {
        try {
            val medicamento = scanMedication(image)
            if (medicamento == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Medicamento nao identificado.") }
            } else {
                _uiState.update { it.copy(isLoading = false, medicamento = medicamento) }
                _events.emit(CameraEvent.NavigateToConfirmation)
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Nao foi possivel analisar a imagem.") }
        }
    }

    private suspend fun queuePhoto(image: ImageReference) {
        try {
            queueOfflineScan(image)
            _uiState.update { it.copy(isLoading = false, showOfflineDialog = false) }
            _events.emit(CameraEvent.OfflineScanQueued)
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Nao foi possivel salvar a imagem.") }
        }
    }

    private fun captureFailed() {
        _uiState.update { it.copy(isLoading = false, errorMessage = "Nao foi possivel capturar a imagem.") }
    }

    private fun selectDose(dose: SelectedDose) {
        savedStateHandle[KEY_MEDICATION_ID] = dose.medicamentoId
        savedStateHandle[KEY_DATE] = dose.data
        savedStateHandle[KEY_TIME] = dose.horario.take(5)
        _uiState.update { it.copy(selectedDose = dose.copy(horario = dose.horario.take(5))) }
    }

    private fun savedDose(): SelectedDose? {
        val id = savedStateHandle.get<Long>(KEY_MEDICATION_ID)
        val date = savedStateHandle.get<String>(KEY_DATE)
        val time = savedStateHandle.get<String>(KEY_TIME)
        return if (id != null && date != null && time != null) SelectedDose(id, date, time) else null
    }

    private companion object {
        const val KEY_MEDICATION_ID = "camera.medicationId"
        const val KEY_DATE = "camera.date"
        const val KEY_TIME = "camera.time"
    }
}

data class CameraUiState(
    val medicamento: MedicamentoCapturadoDomain? = null,
    val capturedPhoto: ImageReference? = null,
    val selectedDose: SelectedDose? = null,
    val isLoading: Boolean = false,
    val showOfflineDialog: Boolean = false,
    val errorMessage: String? = null,
)

data class SelectedDose(val medicamentoId: Long, val data: String, val horario: String)

sealed interface CameraIntent {
    data class RequestCapture(val isOnline: Boolean) : CameraIntent
    data class PhotoCaptured(val image: ImageReference, val offline: Boolean) : CameraIntent
    data class SelectDose(val dose: SelectedDose) : CameraIntent
    data class UpdateMedication(val medicamento: MedicamentoCapturadoDomain) : CameraIntent
    data object CaptureFailed : CameraIntent
    data object ConfirmOfflineCapture : CameraIntent
    data object DismissOfflineDialog : CameraIntent
}

sealed interface CameraEvent {
    data class CapturePhoto(val offline: Boolean) : CameraEvent
    data object NavigateToConfirmation : CameraEvent
    data object OfflineScanQueued : CameraEvent
}
