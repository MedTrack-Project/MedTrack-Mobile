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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medtrack.mobile.R
import com.medtrack.mobile.ui.components.EntradaDeTexto
import com.medtrack.mobile.ui.screen.viewmodel.LoginEvent
import com.medtrack.mobile.ui.screen.viewmodel.LoginIntent
import com.medtrack.mobile.ui.screen.viewmodel.LoginUiState
import com.medtrack.mobile.ui.screen.viewmodel.LoginViewModel

@Composable
fun TelaLogin(loginViewModel: LoginViewModel, onLoginSuccess: () -> Unit, onForgotPasswordClick: () -> Unit) {
    val state by loginViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(loginViewModel) {
        loginViewModel.events.collect { event ->
            if (event == LoginEvent.Authenticated) onLoginSuccess()
        }
    }
    LoginContent(state, loginViewModel::onIntent, onForgotPasswordClick)
}

@Composable
fun LoginContent(state: LoginUiState, onIntent: (LoginIntent) -> Unit, onForgotPasswordClick: () -> Unit) {
    var username by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var password by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    val isError = state.errorMessage != null

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
        ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier.size(
                        60.dp,
                    ).background(MaterialTheme.colorScheme.primary, CircleShape).padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.medtrack_white_icon),
                        contentDescription = "MedTrack",
                        tint = Color.White,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "Entrar",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                    modifier = Modifier.semantics { heading() },
                )
                Text("Preencha os campos abaixo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EntradaDeTexto("Usuário", username, {
                        username = it
                        onIntent(LoginIntent.ClearError)
                    }, isError = isError)
                    EntradaDeTexto("Senha", password, {
                        password = it
                        onIntent(LoginIntent.ClearError)
                    }, isPassword = true, isError = isError)
                }
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(Modifier.height(40.dp))
                Button(
                    onClick = { onIntent(LoginIntent.Submit(username, password)) },
                    enabled = !state.isLoading && username.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                TextButton(onClick = onForgotPasswordClick, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Esqueceu sua senha?", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
