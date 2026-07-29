package com.example.piec_1.ui.screen
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.piec_1.ui.components.OverlayCamera
import com.example.piec_1.ui.screen.viewModel.CameraViewModel
import com.example.piec_1.utils.connection.ConnectivityObserver

@Composable
fun TelaCamera(
    onBackClick: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
    connectivityObserver: ConnectivityObserver,
) {
    var showOfflineDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val isLoading by viewModel.isLoading.observeAsState(false)
    val framePosition by viewModel.framePosition.observeAsState()
    val isRectangleDetected by viewModel.isRectangleDetected.observeAsState(false)
    val isWifi by connectivityObserver.isWifiAvailable.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        viewModel.startCamera(previewView, lifecycleOwner)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        OverlayCamera(
            isRectangleDetected = isRectangleDetected,
            framePosition = framePosition,
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .size(80.dp)
                .background(
                    color = if (isRectangleDetected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    } else {
                        Color.White.copy(alpha = 0.2f)
                    },
                    shape = CircleShape,
                )
                .clickable(enabled = isRectangleDetected) {
                    if (isWifi) {
                        // Online - captura e envia
                        viewModel.capturePhoto(true)
                    } else {
                        // Offline - mostra diálogo de confirmação
                        showOfflineDialog = true
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = if (isRectangleDetected) MaterialTheme.colorScheme.primary else Color.White,
                border = BorderStroke(4.dp, Color.Black.copy(alpha = 0.1f)),
            ) {}
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 4.dp,
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Processando...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (showOfflineDialog) {
            AlertDialog(
                onDismissRequest = { showOfflineDialog = false },
                title = {
                    Text(
                        text = "Você está offline 📶",
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = "Deseja salvar a foto do medicamento para processar automaticamente " +
                            "quando o Wi-Fi voltar?",
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showOfflineDialog = false
                            viewModel.processOfflinePhoto()
                        },
                    ) {
                        Text("Salvar para depois", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOfflineDialog = false }) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}
