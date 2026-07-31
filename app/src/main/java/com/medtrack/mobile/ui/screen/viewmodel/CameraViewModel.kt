package com.medtrack.mobile.ui.screen.viewmodel

import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.mobile.data.repository.ScanRepository
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.service.CameraService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val cameraService: CameraService,
) : ViewModel() {

    private val _medicamento = MutableLiveData<MedicamentoCapturadoDomain?>()
    val medicamento: LiveData<MedicamentoCapturadoDomain?> = _medicamento

    private val _capturedPhotoUri = MutableLiveData<Uri?>()
    val capturedPhotoUri: LiveData<Uri?> = _capturedPhotoUri

    private val _selectedDose = MutableLiveData<SelectedDose?>()
    val selectedDose: LiveData<SelectedDose?> = _selectedDose

    private val _framePosition = MutableLiveData<Rect?>()
    val framePosition: LiveData<Rect?> get() = _framePosition

    private val _isRectangleDetected = MutableLiveData(false)
    val isRectangleDetected: LiveData<Boolean> get() = _isRectangleDetected

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _showOfflineDialog = MutableLiveData(false)
    val showOfflineDialog: LiveData<Boolean> get() = _showOfflineDialog

    private val _navigateToConfirmation = MutableLiveData(false)
    val navigateToConfirmation: LiveData<Boolean> get() = _navigateToConfirmation

    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraService.startCamera(previewView, lifecycleOwner) { detected, detectedRect ->
            _isRectangleDetected.postValue(detected)
            _framePosition.postValue(detectedRect)
        }
    }

    fun capturePhoto(isOnline: Boolean) {
        if (!isOnline) {
            _showOfflineDialog.postValue(true)
            return
        }

        _isLoading.postValue(true)
        cameraService.capturePhotoOnly { uri ->
            if (uri != null) {
                processOnlinePhoto(uri)
            } else {
                _isLoading.postValue(false)
                Log.e("CameraVM", "Erro ao capturar imagem")
            }
        }
    }

    fun processOfflinePhoto() {
        _isLoading.postValue(true)
        cameraService.capturePhotoOnly { uri ->
            if (uri != null) {
                saveForLater(uri)
            } else {
                _isLoading.postValue(false)
                Log.e("CameraVM", "Erro ao capturar imagem offline")
            }
        }
    }

    private fun processOnlinePhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                _capturedPhotoUri.postValue(uri)
                val file = File(uri.path.orEmpty())
                val medicamento = scanRepository.scanMedicamento(file)

                _isLoading.postValue(false)

                if (medicamento != null) {
                    _medicamento.postValue(medicamento)
                    _navigateToConfirmation.postValue(true)
                } else {
                    Log.e("CameraVM", "Erro na analise da IA")
                }
            } catch (_: Exception) {
                _isLoading.postValue(false)
                Log.e("CameraVM", "Erro no processamento online")
            }
        }
    }

    fun saveForLater(uri: Uri) {
        viewModelScope.launch {
            try {
                scanRepository.salvarScanOffline(uri)
                _showOfflineDialog.postValue(false)
                _isLoading.postValue(false)
            } catch (_: Exception) {
                Log.e("CameraVM", "Erro ao salvar scan offline")
                _isLoading.postValue(false)
            }
        }
    }

    fun dismissOfflineDialog() {
        _showOfflineDialog.postValue(false)
    }

    fun atualizarMedicamento(novoMedicamento: MedicamentoCapturadoDomain) {
        _medicamento.value = novoMedicamento
    }

    fun selecionarDose(medicamentoId: Long, data: String, horario: String) {
        _selectedDose.value = SelectedDose(
            medicamentoId = medicamentoId,
            data = data,
            horario = horario.take(5),
        )
    }

    fun onNavigationToConfirmationHandled() {
        _navigateToConfirmation.value = false
    }
}

data class SelectedDose(val medicamentoId: Long, val data: String = LocalDate.now().toString(), val horario: String)
