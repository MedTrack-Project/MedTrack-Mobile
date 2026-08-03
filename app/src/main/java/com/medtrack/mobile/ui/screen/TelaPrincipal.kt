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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medtrack.mobile.R
import com.medtrack.mobile.domain.model.MedicationItem
import com.medtrack.mobile.ui.components.ListaHorarios
import com.medtrack.mobile.ui.screen.viewmodel.LoginIntent
import com.medtrack.mobile.ui.screen.viewmodel.LoginUiState
import com.medtrack.mobile.ui.screen.viewmodel.LoginViewModel

@Composable
fun TelaPrincipal(loginViewModel: LoginViewModel, onHorarioClick: (MedicationItem) -> Unit) {
    val state by loginViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.hasContent) {
        if (state.hasContent) loginViewModel.onIntent(LoginIntent.RefreshConfirmedDoses)
    }

    PrincipalContent(state, onHorarioClick)
}

@Suppress("LongMethod") // Cabeçalho e agenda compõem uma única árvore declarativa da tela inicial.
@Composable
fun PrincipalContent(state: LoginUiState, onHorarioClick: (MedicationItem) -> Unit) {
    val usuario = state.usuario
    val medicamentos = state.medicamentos
    val dosesConfirmadas = state.dosesConfirmadas

    if (state.isLoading || usuario == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = state.errorMessage ?: "Buscando dados do usuario...",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                    ),
                ),
            ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.medtrack_white_icon),
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.user_icon),
                        contentDescription = "Perfil",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(45.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Ola, ${usuario.nome}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Seus medicamentos da semana",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (medicamentos.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum medicamento para exibir")
                        }
                    } else {
                        ListaHorarios(
                            medicamentos = medicamentos,
                            dosesConfirmadas = dosesConfirmadas,
                            onHorarioClick = onHorarioClick,
                        )
                    }
                }
            }
        }
    }
}
