package com.example.piec_1.ui.screen

import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.piec_1.ui.screen.viewModel.LoginViewModel

@Composable
fun TelaLogin(loginViewModel: LoginViewModel, onLoginSuccess: () -> Unit, onForgotPasswordClick: () -> Unit) {
    val loginResponse = loginViewModel.loginResponse.observeAsState().value
    val errorMessage = loginViewModel.errorMessage.observeAsState().value

    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    val onLoginClick = {
        Log.d("Login", "Username: ${username.value}, Password: ${password.value}")
        loginViewModel.login(username.value, password.value)
    }

    val isError = errorMessage != null

    LaunchedEffect(loginResponse) {
        if (loginResponse != null) {
            onLoginSuccess()
        }
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
                        contentDescription = "Ícone MedTrack",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                )
                Text(
                    text = "Preencha os campos abaixo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EntradaDeTexto(
                        label = "Usuário",
                        text = username.value,
                        onTextChange = { username.value = it },
                        isError = isError,
                    )
                    EntradaDeTexto(
                        label = "Senha",
                        text = password.value,
                        onTextChange = { password.value = it },
                        isPassword = true,
                        isError = isError,
                    )
                }

                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onLoginClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        text = "Esqueceu sua senha?",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
