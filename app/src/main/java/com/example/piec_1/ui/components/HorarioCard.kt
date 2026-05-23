package com.example.piec_1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.piec_1.domain.model.DoseStatus
import com.example.piec_1.domain.model.MedicationItem

@Composable
fun HorarioCard(
    item: MedicationItem,
    onClick: (MedicationItem) -> Unit
) {
    val statusStyle = doseStatusStyle(item.status)
    val isEnabled = item.status != DoseStatus.FUTURE && item.status != DoseStatus.CONFIRMED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = isEnabled) { onClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusStyle.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nomeExibicao,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = statusStyle.contentColor
                )
                if (item.isGenerico) {
                    Text(
                        text = "Medicamento Generico",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusStyle.secondaryContentColor
                    )
                }
                Text(
                    text = item.dosagem,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusStyle.secondaryContentColor
                )
                Text(
                    text = statusStyle.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusStyle.contentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = statusStyle.icon,
                    contentDescription = statusStyle.label,
                    tint = statusStyle.contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = item.horario,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusStyle.contentColor
                )
            }
        }
    }
}

@Composable
private fun doseStatusStyle(status: DoseStatus): DoseStatusStyle {
    return when (status) {
        DoseStatus.FUTURE -> DoseStatusStyle(
            label = "Liberado no horario",
            icon = Icons.Default.Lock,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            secondaryContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
        )
        DoseStatus.AVAILABLE -> DoseStatusStyle(
            label = "Disponivel para confirmar",
            icon = Icons.Default.Schedule,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            secondaryContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DoseStatus.LATE -> DoseStatusStyle(
            label = "Dose atrasada",
            icon = Icons.Default.Warning,
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.error,
            secondaryContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
        )
        DoseStatus.CONFIRMED -> DoseStatusStyle(
            label = "Dose confirmada",
            icon = Icons.Default.CheckCircle,
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF2E7D32),
            secondaryContentColor = Color(0xFF2E7D32).copy(alpha = 0.78f)
        )
        DoseStatus.EXPIRED -> DoseStatusStyle(
            label = "Horario expirado",
            icon = Icons.Default.Warning,
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.error,
            secondaryContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
        )
    }
}

private data class DoseStatusStyle(
    val label: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val secondaryContentColor: Color
)
