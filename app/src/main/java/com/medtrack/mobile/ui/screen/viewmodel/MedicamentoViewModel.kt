package com.medtrack.mobile.ui.screen.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.domain.error.ConfirmationAlreadyExistsException
import com.medtrack.mobile.domain.error.DoseOutsideAllowedTimeException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.MedicationNotFoundException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.usecase.ConfirmMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MedicamentoViewModel @Inject constructor(private val confirmMedicationUseCase: ConfirmMedicationUseCase) :
    ViewModel() {

    private val _uiState = MutableLiveData<MedicamentoUIState>(MedicamentoUIState.Idle)
    val uiState: LiveData<MedicamentoUIState> get() = _uiState

    sealed class MedicamentoUIState {
        object Idle : MedicamentoUIState()
        object Loading : MedicamentoUIState()
        data class Success(val message: String) : MedicamentoUIState()
        data class Error(val message: String) : MedicamentoUIState()
    }

    fun confirmMedication(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        selectedDose: SelectedDose?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.value = MedicamentoUIState.Loading

            try {
                confirmMedicationUseCase(
                    ConfirmationCommand(
                        medicamentoCapturado = medicamentoCapturado,
                        comprovanteImagem = comprovanteImagemUri?.toString()?.let(::ImageReference),
                        medicamentoSelecionadoId = selectedDose?.medicamentoId,
                        dataSelecionada = selectedDose?.data,
                        horarioSelecionado = selectedDose?.horario,
                    ),
                )
                _uiState.value = MedicamentoUIState.Success("Medicamento confirmado!")
                onSuccess()
            } catch (_: InvalidSessionException) {
                val message = "Sessao expirada. Faca login novamente."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (_: MedicationNotFoundException) {
                val message = "Medicamento nao cadastrado. Cadastre-o primeiro."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (_: ConfirmationAlreadyExistsException) {
                val message = "Ja existe uma confirmacao para este horario."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (_: DoseOutsideAllowedTimeException) {
                val message = "Esta dose so pode ser confirmada no horario correto."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (_: Exception) {
                val message = "Erro ao confirmar. Tente novamente."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            }
        }
    }
}
