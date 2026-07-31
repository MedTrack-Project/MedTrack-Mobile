package com.medtrack.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain

@Composable
fun StatusCard(medicamento: MedicamentoCapturadoDomain, isSuccess: Boolean) {
    val bgColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val strokeColor = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, strokeColor),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = strokeColor,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = if (isSuccess) "Identificado com Sucesso!" else "Falha na Identificação",
                style = MaterialTheme.typography.titleMedium,
                color = strokeColor,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isSuccess) {
                // Detalhes do Medicamento
                Column(Modifier.fillMaxWidth()) {
                    Text("Nome: ${medicamento.nome}")
                    Text("Composto: ${medicamento.compostoAtivo}")
                    Text("Dosagem: ${medicamento.dosagem}")
                    Text("Quantidade: ${medicamento.quantidade}")
                    Text("Validade: ${medicamento.validade}")
                }
            } else {
                Text(
                    "Dica: Tente focar melhor o texto e evite reflexos.",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
