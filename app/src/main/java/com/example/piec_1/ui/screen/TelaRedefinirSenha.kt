package com.example.piec_1.ui.screen

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piec_1.R
import com.example.piec_1.ui.components.EntradaDeTexto

@Composable
fun TelaRedefinirSenha(onPasswordReset: () -> Unit) {
    val codigo = remember { mutableStateOf("") }
    val novaSenha = remember { mutableStateOf("") }
    val repetirNovaSenha = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

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
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.medtrack_white_icon),
                        contentDescription = null,
                        tint = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Nova Senha",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                )

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Código enviado para o seu e-mail.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EntradaDeTexto(
                        label = "Código de 6 dígitos",
                        text = codigo.value,
                        onTextChange = { codigo.value = it },
                        isError = errorMessage.value != null,
                    )
                    EntradaDeTexto(
                        label = "Nova senha",
                        text = novaSenha.value,
                        onTextChange = { novaSenha.value = it },
                        isPassword = true,
                        isError = errorMessage.value != null,
                    )
                    EntradaDeTexto(
                        label = "Repita a nova senha",
                        text = repetirNovaSenha.value,
                        onTextChange = { repetirNovaSenha.value = it },
                        isPassword = true,
                        isError = errorMessage.value != null,
                    )
                }

                if (errorMessage.value != null) {
                    Text(
                        text = errorMessage.value!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.Start),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (codigo.value.isBlank() ||
                            novaSenha.value.isBlank() ||
                            repetirNovaSenha.value.isBlank()
                        ) {
                            errorMessage.value = "Preencha todos os campos."
                        } else if (novaSenha.value != repetirNovaSenha.value) {
                            errorMessage.value = "As senhas não coincidem."
                        } else {
                            errorMessage.value = null
                            onPasswordReset()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text("Salvar Nova Senha", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
