package com.example.piec_1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.domain.model.MedicationItem
import com.example.piec_1.domain.usecase.organizeMedicationsByDay
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ListaHorarios(
    medicamentos: List<MedicamentoDomain>,
    dosesConfirmadas: Set<String>,
    onHorarioClick: (MedicationItem) -> Unit
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }

    val medicamentosAgrupados = remember(medicamentos, dosesConfirmadas, now) {
        organizeMedicationsByDay(
            medicamentos = medicamentos,
            currentDate = LocalDate.now(),
            confirmedDoseKeys = dosesConfirmadas,
            now = now
        )
    }

    if (medicamentos.isEmpty()) {
        EmptyCard()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            medicamentosAgrupados.forEach { (date, itemsDoDia) ->
                stickyHeader {
                    DayHeader(date)
                }

                items(
                    items = itemsDoDia,
                    key = { item -> item.id }
                ) { item ->
                    HorarioCard(
                        item = item,
                        onClick = onHorarioClick
                    )
                }
            }
        }
    }
}

@Composable
fun DayHeader(date: LocalDate) {
    val title = when {
        date.isEqual(LocalDate.now()) -> "Hoje"
        date.isEqual(LocalDate.now().plusDays(1)) -> "Amanha"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM"))
    }

    Text(
        text = title.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    )
}
