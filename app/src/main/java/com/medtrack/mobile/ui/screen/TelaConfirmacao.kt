package com.medtrack.mobile.ui.screen

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.ui.components.EntradaDeTexto
import com.medtrack.mobile.ui.components.MedTrackDialog
import com.medtrack.mobile.ui.components.StatusCard
import com.medtrack.mobile.ui.screen.viewmodel.CameraIntent
import com.medtrack.mobile.ui.screen.viewmodel.CameraViewModel
import com.medtrack.mobile.ui.screen.viewmodel.MedicamentoEvent
import com.medtrack.mobile.ui.screen.viewmodel.MedicamentoIntent
import com.medtrack.mobile.ui.screen.viewmodel.MedicamentoUiState
import com.medtrack.mobile.ui.screen.viewmodel.MedicamentoViewModel

@Suppress("LongMethod") // Estados e ações pertencem à mesma árvore declarativa de confirmação.
@Composable
fun TelaConfirmacao(
    cameraViewModel: CameraViewModel,
    medicamentoViewModel: MedicamentoViewModel,
    onConfirmSuccess: () -> Unit,
    onRetakePhoto: () -> Unit,
) {
    val cameraState by cameraViewModel.uiState.collectAsStateWithLifecycle()
    val confirmationState by medicamentoViewModel.uiState.collectAsStateWithLifecycle()
    val medicamento = cameraState.medicamento
    val capturedPhoto = cameraState.capturedPhoto
    val selectedDose = cameraState.selectedDose
    var showEditDialog by remember { mutableStateOf(false) }
    val loading = confirmationState == MedicamentoUiState.Loading
    val errorMessage = (confirmationState as? MedicamentoUiState.Error)?.message
    val medicamentoEditavel = remember(medicamento) {
        mutableStateOf(medicamento ?: medicamentoDesconhecido)
    }

    LaunchedEffect(medicamentoViewModel) {
        medicamentoViewModel.events.collect { event ->
            if (event == MedicamentoEvent.Confirmed) onConfirmSuccess()
        }
    }

    if (medicamento == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isSuccess = verificarMedicamento(medicamento)

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
            ),
        ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StatusCard(medicamento, isSuccess)

                Spacer(modifier = Modifier.height(32.dp))

                if (isSuccess) {
                    Button(
                        onClick = {
                            medicamentoViewModel.onIntent(
                                MedicamentoIntent.Confirm(
                                    ConfirmationCommand(
                                        medicamentoCapturado = medicamento,
                                        comprovanteImagem = capturedPhoto,
                                        medicamentoSelecionadoId = selectedDose?.medicamentoId,
                                        dataSelecionada = selectedDose?.data,
                                        horarioSelecionado = selectedDose?.horario,
                                    ),
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !loading,
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
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
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
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
                cameraViewModel.onIntent(CameraIntent.UpdateMedication(medicamentoEditavel.value))
                showEditDialog = false
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EntradaDeTexto(
                    label = "Nome do Medicamento",
                    text = medicamentoEditavel.value.nome,
                    onTextChange = {
                        medicamentoEditavel.value =
                            medicamentoEditavel.value.copy(nome = it)
                    },
                )
                EntradaDeTexto(
                    label = "Composto Ativo",
                    text = medicamentoEditavel.value.compostoAtivo,
                    onTextChange = {
                        medicamentoEditavel.value =
                            medicamentoEditavel.value.copy(compostoAtivo = it)
                    },
                )
                EntradaDeTexto(
                    label = "Dosagem",
                    text = medicamentoEditavel.value.dosagem,
                    onTextChange = {
                        medicamentoEditavel.value =
                            medicamentoEditavel.value.copy(dosagem = it)
                    },
                )
                EntradaDeTexto(
                    label = "Quantidade",
                    text = medicamentoEditavel.value.quantidade,
                    onTextChange = {
                        medicamentoEditavel.value =
                            medicamentoEditavel.value.copy(quantidade = it)
                    },
                )
                EntradaDeTexto(
                    label = "Validade",
                    text = medicamentoEditavel.value.validade ?: "",
                    onTextChange = {
                        medicamentoEditavel.value =
                            medicamentoEditavel.value.copy(validade = it)
                    },
                )
            }
        }
    }
}

private fun verificarMedicamento(medicamento: MedicamentoCapturadoDomain): Boolean =
    medicamento.nome != "Desconhecido" &&
        medicamento.compostoAtivo != "Desconhecido" &&
        medicamento.dosagem != "Desconhecido"

private val medicamentoDesconhecido = MedicamentoCapturadoDomain(
    nome = "Desconhecido",
    compostoAtivo = "Desconhecido",
    dosagem = "Desconhecido",
    quantidade = "",
    validade = "",
)
