package com.medtrack.mobile.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.domain.error.InvalidCredentialsException
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario
import com.medtrack.mobile.domain.usecase.GetConfirmedDosesUseCase
import com.medtrack.mobile.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getConfirmedDoses: GetConfirmedDosesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var loginJob: Job? = null

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Submit -> login(intent.username, intent.password)
            LoginIntent.RefreshConfirmedDoses -> loadConfirmedDoses()
            LoginIntent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun login(username: String, password: String) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = loginUseCase(username, password)
                val confirmedDoses = runCatching { getConfirmedDoses() }.getOrDefault(emptySet())
                _uiState.value = LoginUiState(
                    usuario = result.usuario,
                    medicamentos = result.medicamentos,
                    dosesConfirmadas = confirmedDoses,
                )
                _events.emit(LoginEvent.Authenticated)
            } catch (_: InvalidCredentialsException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario ou senha invalidos") }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Erro ao tentar fazer login. Tente novamente")
                }
            }
        }
    }

    private fun loadConfirmedDoses() {
        viewModelScope.launch {
            runCatching { getConfirmedDoses() }
                .onSuccess { doses -> _uiState.update { it.copy(dosesConfirmadas = doses) } }
                .onFailure { /* Mantem as confirmacoes ja apresentadas. */ }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val usuario: Usuario? = null,
    val medicamentos: List<MedicamentoDomain> = emptyList(),
    val dosesConfirmadas: Set<String> = emptySet(),
    val errorMessage: String? = null,
) {
    val hasContent: Boolean get() = usuario != null
}

sealed interface LoginIntent {
    data class Submit(val username: String, val password: String) : LoginIntent
    data object RefreshConfirmedDoses : LoginIntent
    data object ClearError : LoginIntent
}

sealed interface LoginEvent {
    data object Authenticated : LoginEvent
}
