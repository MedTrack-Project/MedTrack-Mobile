package com.medtrack.mobile.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.domain.error.ConfirmationAlreadyExistsException
import com.medtrack.mobile.domain.error.DoseOutsideAllowedTimeException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.MedicationNotFoundException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.usecase.ConfirmMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MedicamentoViewModel @Inject constructor(private val confirmMedication: ConfirmMedicationUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<MedicamentoUiState>(MedicamentoUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MedicamentoEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var confirmationJob: Job? = null

    fun onIntent(intent: MedicamentoIntent) {
        when (intent) {
            is MedicamentoIntent.Confirm -> confirm(intent.command)
            MedicamentoIntent.DismissError -> _uiState.value = MedicamentoUiState.Idle
        }
    }

    private fun confirm(command: ConfirmationCommand) {
        if (confirmationJob?.isActive == true) return
        confirmationJob = viewModelScope.launch {
            _uiState.value = MedicamentoUiState.Loading
            try {
                confirmMedication(command)
                _uiState.value = MedicamentoUiState.Success
                _events.emit(MedicamentoEvent.Confirmed)
            } catch (error: Exception) {
                _uiState.value = MedicamentoUiState.Error(error.toUserMessage())
            }
        }
    }
}

sealed interface MedicamentoUiState {
    data object Idle : MedicamentoUiState
    data object Loading : MedicamentoUiState
    data object Success : MedicamentoUiState
    data class Error(val message: String) : MedicamentoUiState
}

sealed interface MedicamentoIntent {
    data class Confirm(val command: ConfirmationCommand) : MedicamentoIntent
    data object DismissError : MedicamentoIntent
}

sealed interface MedicamentoEvent {
    data object Confirmed : MedicamentoEvent
}

private fun Exception.toUserMessage(): String = when (this) {
    is InvalidSessionException -> "Sessao expirada. Faca login novamente."
    is MedicationNotFoundException -> "Medicamento nao cadastrado. Cadastre-o primeiro."
    is ConfirmationAlreadyExistsException -> "Ja existe uma confirmacao para este horario."
    is DoseOutsideAllowedTimeException -> "Esta dose so pode ser confirmada no horario correto."
    else -> "Erro ao confirmar. Tente novamente."
}
