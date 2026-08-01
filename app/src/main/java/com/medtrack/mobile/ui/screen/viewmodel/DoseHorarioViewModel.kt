package com.medtrack.mobile.ui.screen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.domain.model.DoseStatus
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.usecase.DoseDetails
import com.medtrack.mobile.domain.usecase.LoadDoseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DoseHorarioViewModel @Inject constructor(private val loadDose: LoadDoseUseCase) : ViewModel() {

    private val _uiState = MutableLiveData<DoseHorarioUiState>(DoseHorarioUiState.Loading)
    val uiState: LiveData<DoseHorarioUiState> get() = _uiState

    fun carregarDose(medicamentoId: Long, data: String, horario: String) {
        viewModelScope.launch {
            _uiState.value = DoseHorarioUiState.Loading

            try {
                _uiState.value = when (val dose = loadDose(medicamentoId, data, horario)) {
                    DoseDetails.NotFound -> DoseHorarioUiState.Error("Medicamento nao encontrado.")
                    is DoseDetails.Found -> DoseHorarioUiState.Success(dose.medicamento, dose.status)
                }
            } catch (_: Exception) {
                _uiState.value = DoseHorarioUiState.Error("Nao foi possivel carregar a dose.")
            }
        }
    }
}

sealed class DoseHorarioUiState {
    data object Loading : DoseHorarioUiState()
    data class Success(val medicamento: MedicamentoDomain, val status: DoseStatus) : DoseHorarioUiState()
    data class Error(val message: String) : DoseHorarioUiState()
}
