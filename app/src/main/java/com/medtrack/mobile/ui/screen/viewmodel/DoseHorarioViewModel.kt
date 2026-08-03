package com.medtrack.mobile.ui.screen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.domain.model.DoseStatus
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.usecase.DoseDetails
import com.medtrack.mobile.domain.usecase.LoadDoseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DoseHorarioViewModel @Inject constructor(
    private val loadDose: LoadDoseUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DoseHorarioUiState>(DoseHorarioUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun onIntent(intent: DoseHorarioIntent) {
        when (intent) {
            is DoseHorarioIntent.Load -> load(intent)
            DoseHorarioIntent.Retry -> savedRequest()?.let(::load)
        }
    }

    private fun load(request: DoseHorarioIntent.Load) {
        if (request == savedRequest() && _uiState.value is DoseHorarioUiState.Success) return
        save(request)
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = DoseHorarioUiState.Loading
            _uiState.value = try {
                when (val dose = loadDose(request.medicamentoId, request.data, request.horario)) {
                    DoseDetails.NotFound -> DoseHorarioUiState.Error("Medicamento nao encontrado.")
                    is DoseDetails.Found -> DoseHorarioUiState.Success(dose.medicamento, dose.status)
                }
            } catch (_: Exception) {
                DoseHorarioUiState.Error("Nao foi possivel carregar a dose.")
            }
        }
    }

    private fun save(request: DoseHorarioIntent.Load) {
        savedStateHandle[KEY_MEDICATION_ID] = request.medicamentoId
        savedStateHandle[KEY_DATE] = request.data
        savedStateHandle[KEY_TIME] = request.horario
    }

    private fun savedRequest(): DoseHorarioIntent.Load? {
        val medicationId = savedStateHandle.get<Long>(KEY_MEDICATION_ID)
        val date = savedStateHandle.get<String>(KEY_DATE)
        val time = savedStateHandle.get<String>(KEY_TIME)
        return if (medicationId != null && date != null && time != null) {
            DoseHorarioIntent.Load(medicationId, date, time)
        } else {
            null
        }
    }

    private companion object {
        const val KEY_MEDICATION_ID = "dose.medicationId"
        const val KEY_DATE = "dose.date"
        const val KEY_TIME = "dose.time"
    }
}

sealed interface DoseHorarioIntent {
    data class Load(val medicamentoId: Long, val data: String, val horario: String) : DoseHorarioIntent
    data object Retry : DoseHorarioIntent
}

sealed interface DoseHorarioUiState {
    data object Loading : DoseHorarioUiState
    data class Success(val medicamento: MedicamentoDomain, val status: DoseStatus) : DoseHorarioUiState
    data class Error(val message: String) : DoseHorarioUiState
}
