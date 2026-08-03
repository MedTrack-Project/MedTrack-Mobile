package com.medtrack.mobile.ui.components

import android.graphics.Rect
import android.graphics.RectF
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Suppress("LongMethod") // O Canvas representa uma única operação de desenho e compartilha o mesmo DrawScope.
@Composable
fun OverlayCamera(isRectangleDetected: Boolean, framePosition: Rect? = null) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val overlayAlpha = if (isRectangleDetected) 0.0f else 0.6f

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val rectWidth = 180.dp.toPx()
            val rectHeight = 280.dp.toPx()
            val cornerSize = 24.dp.toPx()
            val strokeWidth = 4.dp.toPx()

            if (!isRectangleDetected) {
                with(drawContext.canvas.nativeCanvas) {
                    val checkpoint = saveLayer(null, null)
                    drawRect(Color.Black.copy(alpha = overlayAlpha))

                    // Desenha o "buraco" transparente no meio
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
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(
                        (canvasWidth - rectWidth) / 2,
                        (canvasHeight - rectHeight) / 2,
                    ),
                    size = Size(rectWidth, rectHeight),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }

            framePosition?.let { rect ->
                val box = RectF(
                    rect.left.toFloat(),
                    rect.top.toFloat(),
                    rect.right.toFloat(),
                    rect.bottom.toFloat(),
                )

                val path = Path().apply {
                    moveTo(box.left, box.top + cornerSize)
                    lineTo(box.left, box.top)
                    lineTo(box.left + cornerSize, box.top)

                    moveTo(box.right - cornerSize, box.top)
                    lineTo(box.right, box.top)
                    lineTo(box.right, box.top + cornerSize)

                    moveTo(box.right, box.bottom - cornerSize)
                    lineTo(box.right, box.bottom)
                    lineTo(box.right - cornerSize, box.bottom)

                    moveTo(box.left + cornerSize, box.bottom)
                    lineTo(box.left, box.bottom)
                    lineTo(box.left, box.bottom - cornerSize)
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }

        if (!isRectangleDetected) {
            Text(
                text = "Aproxime a caixa do remédio",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 240.dp),
            )
        }
    }
}
