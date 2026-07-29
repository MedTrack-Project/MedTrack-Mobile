package com.example.piec_1.ui.screen.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piec_1.data.repository.MedicamentoRepositoryContract
import com.example.piec_1.domain.model.DoseStatus
import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.domain.usecase.resolveDoseStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DoseHorarioViewModel @Inject constructor(private val medicamentoRepository: MedicamentoRepositoryContract) :
    ViewModel() {

    private val _uiState = MutableLiveData<DoseHorarioUiState>(DoseHorarioUiState.Loading)
    val uiState: LiveData<DoseHorarioUiState> get() = _uiState

    fun carregarDose(medicamentoId: Long, data: String, horario: String) {
        viewModelScope.launch {
            _uiState.value = DoseHorarioUiState.Loading

            try {
                val medicamento = medicamentoRepository.buscarMedicamentoLocal(medicamentoId)
                val confirmedDoseKeys = medicamentoRepository.buscarChavesDeDosesConfirmadas()
                val doseDate = LocalDate.parse(data)
                val doseTime = LocalTime.parse(horario)
                _uiState.value = if (medicamento != null) {
                    DoseHorarioUiState.Success(
                        medicamento = medicamento,
                        status = resolveDoseStatus(
                            medicamentoId = medicamentoId,
                            date = doseDate,
                            horario = doseTime,
                            confirmedDoseKeys = confirmedDoseKeys,
                        ),
                    )
                } else {
                    DoseHorarioUiState.Error("Medicamento nao encontrado.")
                }
            } catch (e: Exception) {
                _uiState.value = DoseHorarioUiState.Error(
                    e.message ?: "Nao foi possivel carregar a dose.",
                )
            }
        }
    }
}

sealed class DoseHorarioUiState {
    data object Loading : DoseHorarioUiState()
    data class Success(val medicamento: MedicamentoDomain, val status: DoseStatus) : DoseHorarioUiState()
    data class Error(val message: String) : DoseHorarioUiState()
}
