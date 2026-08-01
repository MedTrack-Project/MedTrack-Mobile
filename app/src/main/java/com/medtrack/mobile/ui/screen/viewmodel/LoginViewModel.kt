package com.medtrack.mobile.ui.screen.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.domain.error.InvalidCredentialsException
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario
import com.medtrack.mobile.domain.usecase.GetConfirmedDosesUseCase
import com.medtrack.mobile.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getConfirmedDoses: GetConfirmedDosesUseCase,
) : ViewModel() {

    private val _loginResponse = MutableLiveData<String>()
    val loginResponse: LiveData<String> get() = _loginResponse

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> get() = _usuario

    private val _medicamentos = MutableLiveData<List<MedicamentoDomain>>()
    val medicamentos: LiveData<List<MedicamentoDomain>> get() = _medicamentos

    private val _dosesConfirmadas = MutableLiveData<Set<String>>(emptySet())
    val dosesConfirmadas: LiveData<Set<String>> get() = _dosesConfirmadas

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val loginData = loginUseCase(username, password)
                _usuario.postValue(loginData.usuario)
                _medicamentos.postValue(loginData.medicamentos)
                carregarDosesConfirmadas()
                _loginResponse.postValue(loginData.token)
            } catch (_: InvalidCredentialsException) {
                Log.w("Login", "Falha de autenticacao")
                _errorMessage.postValue("Usuario ou senha invalidos")
            } catch (_: Exception) {
                Log.e("Login", "Falha inesperada durante autenticacao")
                _errorMessage.postValue("Erro ao tentar fazer login. Tente novamente")
            }
        }
    }

    fun carregarDosesConfirmadas() {
        viewModelScope.launch {
            try {
                _dosesConfirmadas.postValue(getConfirmedDoses())
            } catch (_: Exception) {
                Log.e("Login", "Falha ao carregar confirmacoes")
            }
        }
    }
}
