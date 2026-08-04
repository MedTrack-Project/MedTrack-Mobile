package com.medtrack.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun OverlayCamera() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val rectWidth = 180.dp.toPx()
            val rectHeight = 280.dp.toPx()

            with(drawContext.canvas.nativeCanvas) {
                val checkpoint = saveLayer(null, null)
                drawRect(Color.Black.copy(alpha = 0.6f))
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(
                        (canvasWidth - rectWidth) / 2,
                        (canvasHeight - rectHeight) / 2,
                    ),
                    size = Size(rectWidth, rectHeight),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.Clear,
                )
                restoreToCount(checkpoint)
            }

            drawRoundRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(
                    (canvasWidth - rectWidth) / 2,
                    (canvasHeight - rectHeight) / 2,
                ),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        Text(
            text = "Enquadre a caixa do remédio",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 240.dp),
        )
    }
}
