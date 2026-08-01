package com.medtrack.mobile.data.camera

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import javax.inject.Inject

class DetectionService @Inject constructor() {

    private val objectDetector: ObjectDetector

    init {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .build()

        objectDetector = ObjectDetection.getClient(options)
    }

    fun detectObjects(
        image: Bitmap,
        previewWidth: Int,
        previewHeight: Int,
        onDetectionResult: (Boolean, Rect?) -> Unit,
    ) {
        val inputImage = InputImage.fromBitmap(image, 0)

        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                val rect = detectedObjects
                    .map { it.boundingBox }
                    .find { isRectagle(it, previewWidth, previewHeight) }

                if (rect != null) {
                    val adjustedBounds = adjustBoundingBox(
                        rect,
                        image.width,
                        image.height,
                        previewWidth,
                        previewHeight,
                    )
                    onDetectionResult(true, adjustedBounds)
                } else {
                    onDetectionResult(false, null)
                }
            }
            .addOnFailureListener {
                Log.e("DetectionService", "Erro na deteccao")
                onDetectionResult(false, null)
            }
    }

    private fun isRectagle(rect: Rect, imageWidth: Int, imageHeight: Int): Boolean {
        val width = rect.width().toFloat()
        val height = rect.height().toFloat()
        val aspectRatio = height / width

        val isVertical = aspectRatio in 1.5..3.5
        val isHorizontal = aspectRatio in 0.3..0.7

        val minWidth = imageWidth * 0.2
        val minHeight = imageHeight * 0.2

        return (isVertical || isHorizontal) && (width > minWidth) && (height > minHeight)
    }

    private fun adjustBoundingBox(
        boundingBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
        previewWidth: Int,
        previewHeight: Int,
    ): Rect {
        val scaleX = previewWidth.toFloat() / imageWidth
        val scaleY = previewHeight.toFloat() / imageHeight

        return Rect(
            (boundingBox.left * scaleX).toInt(),
            (boundingBox.top * scaleY).toInt(),
            (boundingBox.right * scaleX).toInt(),
            (boundingBox.bottom * scaleY).toInt(),
        )
    }
}
