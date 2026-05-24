package com.example.piec_1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.ui.components.EntradaDeTexto
import com.example.piec_1.ui.components.MedTrackDialog
import com.example.piec_1.ui.components.StatusCard
import com.example.piec_1.ui.screen.viewModel.CameraViewModel
import com.example.piec_1.ui.screen.viewModel.MedicamentoViewModel

@Composable
fun TelaConfirmacao(
    cameraViewModel: CameraViewModel,
    medicamentoViewModel: MedicamentoViewModel,
    onConfirmSuccess: () -> Unit,
    onRetakePhoto: () -> Unit
) {
    val medicamento by cameraViewModel.medicamento.observeAsState()
    val capturedPhotoUri by cameraViewModel.capturedPhotoUri.observeAsState()
    val selectedDose by cameraViewModel.selectedDose.observeAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val medicamentoEditavel = remember(medicamento) {
        mutableStateOf(medicamento ?: medicamentoDesconhecido)
    }


    if (medicamento == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isSuccess = verificarMedicamento(medicamento!!)

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
        )),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusCard(medicamento!!, isSuccess)

                Spacer(modifier = Modifier.height(32.dp))

                if (isSuccess) {
                    Button(
                        onClick = {
                            loading = true
                            errorMessage = null
                            medicamentoViewModel.confirmarMedicamento(
                                medicamentoCapturado = medicamento!!,
                                comprovanteImagemUri = capturedPhotoUri,
                                selectedDose = selectedDose,
                                onSuccess = {
                                    loading = false
                                    onConfirmSuccess()
                                },
                                onError = { error ->
                                    loading = false
                                    errorMessage = error
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Tudo Certo, Confirmar", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Editar Informações")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onRetakePhoto) {
                    Text("Tirar outra foto", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showEditDialog) {
        MedTrackDialog(
            titulo = "Editar Medicamento",
            onDismiss = { showEditDialog = false },
            onConfirm = {
                cameraViewModel.atualizarMedicamento(medicamentoEditavel.value)
                showEditDialog = false
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EntradaDeTexto(
                    label = "Nome do Medicamento",
                    text = medicamentoEditavel.value.nome,
                    onTextChange = { medicamentoEditavel.value = medicamentoEditavel.value.copy(nome = it) }
                )
                EntradaDeTexto(
                    label = "Composto Ativo",
                    text = medicamentoEditavel.value.compostoAtivo,
                    onTextChange = { medicamentoEditavel.value = medicamentoEditavel.value.copy(compostoAtivo = it) }
                )
                EntradaDeTexto(
                    label = "Dosagem",
                    text = medicamentoEditavel.value.dosagem,
                    onTextChange = { medicamentoEditavel.value = medicamentoEditavel.value.copy(dosagem = it) }
                )
                EntradaDeTexto(
                    label = "Quantidade",
                    text = medicamentoEditavel.value.quantidade,
                    onTextChange = { medicamentoEditavel.value = medicamentoEditavel.value.copy(quantidade = it) }
                )
                EntradaDeTexto(
                    label = "Validade",
                    text = medicamentoEditavel.value.validade ?: "",
                    onTextChange = { medicamentoEditavel.value = medicamentoEditavel.value.copy(validade = it) }
                )
            }
        }
    }
}

private fun verificarMedicamento(medicamento: MedicamentoCapturadoDomain): Boolean {
    return medicamento.nome != "Desconhecido" && medicamento.compostoAtivo != "Desconhecido" &&
            medicamento.dosagem != "Desconhecido"
}

private val medicamentoDesconhecido = MedicamentoCapturadoDomain(
    nome = "Desconhecido",
    compostoAtivo = "Desconhecido",
    dosagem = "Desconhecido",
    quantidade = "",
    validade = ""
)
