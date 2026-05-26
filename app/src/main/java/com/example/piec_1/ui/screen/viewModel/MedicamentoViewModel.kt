package com.example.piec_1.ui.screen.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piec_1.data.repository.MedicamentoRepositoryContract
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.utils.exceptions.ConfirmacaoExistenteException
import com.example.piec_1.utils.exceptions.DoseForaDoHorarioException
import com.example.piec_1.utils.exceptions.MedicamentoNaoEncontradoException
import com.example.piec_1.utils.exceptions.TokenNaoEncontradoException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicamentoViewModel @Inject constructor(
    private val medicamentoRepository: MedicamentoRepositoryContract
) : ViewModel() {

    private val _uiState = MutableLiveData<MedicamentoUIState>(MedicamentoUIState.Idle)
    val uiState: LiveData<MedicamentoUIState> get() = _uiState

    sealed class MedicamentoUIState {
        object Idle : MedicamentoUIState()
        object Loading : MedicamentoUIState()
        data class Success(val message: String) : MedicamentoUIState()
        data class Error(val message: String) : MedicamentoUIState()
    }

    fun confirmarMedicamento(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        selectedDose: SelectedDose?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = MedicamentoUIState.Loading

            try {
                medicamentoRepository.confirmarMedicamento(
                    medicamentoCapturado = medicamentoCapturado,
                    comprovanteImagemUri = comprovanteImagemUri,
                    medicamentoSelecionadoId = selectedDose?.medicamentoId,
                    dataSelecionada = selectedDose?.data,
                    horarioSelecionado = selectedDose?.horario
                )
                _uiState.value = MedicamentoUIState.Success("Medicamento confirmado!")
                onSuccess()
            } catch (_: TokenNaoEncontradoException) {
                val message = "Sessao expirada. Faca login novamente."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (_: MedicamentoNaoEncontradoException) {
                val message = "Medicamento nao cadastrado. Cadastre-o primeiro."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (_: ConfirmacaoExistenteException) {
                val message = "Ja existe uma confirmacao para este horario."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (_: DoseForaDoHorarioException) {
                val message = "Esta dose so pode ser confirmada no horario correto."
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            } catch (e: Exception) {
                val message = "Erro ao confirmar: ${e.message ?: "Tente novamente"}"
                _uiState.value = MedicamentoUIState.Error(message)
                onError(message)
            }
        }
    }
}
