package com.medtrack.mobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.medtrack.mobile.R
import com.medtrack.mobile.domain.model.DoseStatus
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.ui.components.MedicamentoImage
import com.medtrack.mobile.ui.screen.viewmodel.DoseHorarioUiState
import com.medtrack.mobile.ui.screen.viewmodel.DoseHorarioViewModel
import com.medtrack.mobile.utils.formatarHorario

@Composable
fun TelaDoseHorario(
    medicamentoId: Long,
    data: String,
    horario: String,
    onBackClick: () -> Unit,
    onScanClick: () -> Unit,
    viewModel: DoseHorarioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.observeAsState(DoseHorarioUiState.Loading)

    LaunchedEffect(medicamentoId, data, horario) {
        viewModel.carregarDose(medicamentoId, data, horario)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
                ),
            ),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            when (val state = uiState) {
                DoseHorarioUiState.Loading -> LoadingDose()
                is DoseHorarioUiState.Error -> ErrorDose(state.message)
                is DoseHorarioUiState.Success -> DoseContent(
                    medicamento = state.medicamento,
                    horario = horario,
                    status = state.status,
                    onScanClick = onScanClick,
                )
            }
        }
    }
}

@Composable
private fun LoadingDose() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorDose(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun DoseContent(medicamento: MedicamentoDomain, horario: String, status: DoseStatus, onScanClick: () -> Unit) {
    val scanEnabled = status != DoseStatus.FUTURE && status != DoseStatus.CONFIRMED
    val statusColor = when (status) {
        DoseStatus.LATE -> MaterialTheme.colorScheme.error
        DoseStatus.CONFIRMED -> Color(0xFF2E7D32)
        DoseStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
        DoseStatus.FUTURE -> MaterialTheme.colorScheme.onSurfaceVariant
        DoseStatus.EXPIRED -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Dose das ${formatarHorario(horario)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        MedicamentoImage(
            imagemUrl = medicamento.imagemUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = nomeExibicao(medicamento),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = medicamento.compostoAtivo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = statusColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Dosagem",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    Text(
                        text = medicamento.dosagem,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Horario",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    Text(
                        text = formatarHorario(horario),
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                    )
                }
            }
        }

        Text(
            text = statusMessage(status),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = statusColor,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (status != DoseStatus.CONFIRMED) {
            Button(
                onClick = onScanClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp),
                enabled = scanEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status == DoseStatus.LATE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Abrir Câmera",
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Escanear Medicamento", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun statusMessage(status: DoseStatus): String = when (status) {
    DoseStatus.FUTURE -> "Esta dose ainda não foi liberada."
    DoseStatus.AVAILABLE -> "Dose liberada para confirmação."
    DoseStatus.LATE -> "Dose atrasada. Confirme nos próximos minutos."
    DoseStatus.CONFIRMED -> "Esta dose já foi confirmada."
    DoseStatus.EXPIRED -> "Dose em atraso, mas ainda é possivel confirmar."
}

private fun nomeExibicao(medicamento: MedicamentoDomain): String = if (
    medicamento.nome.equals("MEDICAMENTO GENERICO", ignoreCase = true) ||
    medicamento.nome.equals("MEDICAMENTO GENÉRICO", ignoreCase = true)
) {
    medicamento.compostoAtivo
} else {
    medicamento.nome
}
