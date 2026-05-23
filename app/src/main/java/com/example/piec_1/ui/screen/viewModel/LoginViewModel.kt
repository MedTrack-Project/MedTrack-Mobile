package com.example.piec_1.ui.screen.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piec_1.data.repository.AuthRepository
import com.example.piec_1.data.repository.LoginException
import com.example.piec_1.data.repository.MedicamentoRepository
import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.domain.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val medicamentoRepository: MedicamentoRepository
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
                val token = authRepository.login(username, password)
                val loginData = medicamentoRepository.sincronizarDadosDoUsuario(token)
                Log.d("Login", "Token: ${loginData.token}")
                _usuario.postValue(loginData.usuario)
                _medicamentos.postValue(loginData.medicamentos)
                carregarDosesConfirmadas()
                _loginResponse.postValue(loginData.token)
            } catch (e: LoginException) {
                Log.e("Login", "Erro de login: ${e.message}")
                _errorMessage.postValue(e.message ?: "Usuario ou senha invalidos")
            } catch (e: Exception) {
                Log.e("Login", "Exception: ${e.message}")
                _errorMessage.postValue("Erro ao tentar fazer login. Tente novamente")
            }
        }
    }

    fun carregarDosesConfirmadas() {
        viewModelScope.launch {
            try {
                _dosesConfirmadas.postValue(medicamentoRepository.buscarChavesDeDosesConfirmadas())
            } catch (e: Exception) {
                Log.e("Login", "Erro ao carregar confirmacoes: ${e.message}")
            }
        }
    }
}
