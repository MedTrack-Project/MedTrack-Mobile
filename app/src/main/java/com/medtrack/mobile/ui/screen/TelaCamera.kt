package com.medtrack.mobile.ui.screen

import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.ui.camera.CameraController
import com.medtrack.mobile.ui.components.OverlayCamera
import com.medtrack.mobile.ui.screen.viewmodel.CameraEvent
import com.medtrack.mobile.ui.screen.viewmodel.CameraIntent
import com.medtrack.mobile.ui.screen.viewmodel.CameraViewModel
import com.medtrack.mobile.utils.connection.ConnectivityObserver

@Suppress("LongMethod") // Preview, overlay e controles formam uma única composição da câmera.
@Composable
fun TelaCamera(
    onBackClick: () -> Unit,
    viewModel: CameraViewModel,
    cameraController: CameraController,
    connectivityObserver: ConnectivityObserver,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isWifi by connectivityObserver.isWifiAvailable.collectAsStateWithLifecycle(false)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(previewView, lifecycleOwner) {
        cameraController.startCamera(previewView, lifecycleOwner)
        onDispose { }
    }
    LaunchedEffect(viewModel, cameraController) {
        viewModel.events.collect { event ->
            if (event is CameraEvent.CapturePhoto) {
                cameraController.capturePhotoOnly { uri ->
                    viewModel.onIntent(
                        uri?.let { CameraIntent.PhotoCaptured(ImageReference(it.toString()), event.offline) }
                            ?: CameraIntent.CaptureFailed,
                    )
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        OverlayCamera()
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(
                top = 40.dp,
                start = 16.dp,
            ).background(Color.Black.copy(alpha = .3f), CircleShape),
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White) }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp).size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = .3f),
                    CircleShape,
                )
                .semantics {
                    role = Role.Button
                    contentDescription = "Capturar medicamento"
                }
                .clickable(enabled = !state.isLoading) {
                    viewModel.onIntent(CameraIntent.RequestCapture(isWifi))
                },
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(4.dp, Color.Black.copy(alpha = .1f)),
            ) {}
        }
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .6f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("Processando...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        state.errorMessage?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 96.dp),
            )
        }
        if (state.showOfflineDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onIntent(CameraIntent.DismissOfflineDialog) },
                title = { Text("Você está offline", fontWeight = FontWeight.Bold) },
                text = { Text("Deseja salvar a foto para processar automaticamente quando a conexão voltar?") },
                icon = {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onIntent(CameraIntent.ConfirmOfflineCapture)
                    }) { Text("Salvar para depois") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onIntent(CameraIntent.DismissOfflineDialog) }) { Text("Cancelar") }
                },
            )
        }
    }
}
