package com.example.piec_1.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.piec_1.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun MedicamentoImage(
    imagemUrl: String?,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(imagemUrl) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(imagemUrl) { mutableStateOf(!imagemUrl.isNullOrBlank()) }
    var showExpandedImage by remember { mutableStateOf(false) }

    LaunchedEffect(imagemUrl) {
        bitmap = null
        isLoading = !imagemUrl.isNullOrBlank()

        if (!imagemUrl.isNullOrBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    URL(imagemUrl).openStream().use(BitmapFactory::decodeStream)
                }.getOrNull()
            }
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .clickable(enabled = bitmap != null) { showExpandedImage = true },
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Imagem do medicamento",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            isLoading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Medicamento sem imagem",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }

    if (showExpandedImage && bitmap != null) {
        ExpandedMedicationImage(
            bitmap = bitmap!!,
            onDismiss = { showExpandedImage = false }
        )
    }
}

@Composable
private fun ExpandedMedicationImage(
    bitmap: Bitmap,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Imagem ampliada do medicamento",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
                    .transformable(transformState),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar imagem",
                    tint = Color.White
                )
            }
        }
    }
}
