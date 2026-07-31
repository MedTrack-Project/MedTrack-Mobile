package com.medtrack.mobile.domain.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

class CameraService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val detectionService: DetectionService,
) {
    private var imageCapture: ImageCapture? = null

    fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onObjectDetected: (Boolean, Rect?) -> Unit,
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val previewWidth = previewView.width
            val previewHeight = previewView.height

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processFrame(imageProxy, previewWidth, previewHeight, onObjectDetected)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis,
                    imageCapture,
                )
            } catch (_: Exception) {
                Log.e("CameraX", "Erro ao iniciar camera")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun capturePhotoOnly(onImageCaptured: (Uri?) -> Unit) {
        val photoFile = File(
            context.filesDir,
            "scan_${System.currentTimeMillis()}.jpg",
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("CameraService", "Foto salva com sucesso")
                    onImageCaptured(Uri.fromFile(photoFile))
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraService", "Erro ao salvar foto")
                    onImageCaptured(null)
                }
            },
        )
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processFrame(
        imageProxy: ImageProxy,
        previewWidth: Int,
        previewHeight: Int,
        onObjectDetected: (Boolean, Rect?) -> Unit,
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmap = mediaImage.toBitmap(rotationDegrees)

            detectionService.detectObjects(bitmap, previewWidth, previewHeight) {
                    detected,
                    objectBounds,
                ->
                onObjectDetected(detected, objectBounds)
            }
        }
        imageProxy.close()
    }

    private fun Image.toBitmap(rotationDegrees: Int): Bitmap {
        val yuvBytes = yuv420ToNv21(this)
        val yuvImage = YuvImage(yuvBytes, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun yuv420ToNv21(image: Image): ByteArray {
        val yPlane = image.planes[0].buffer
        val uPlane = image.planes[1].buffer
        val vPlane = image.planes[2].buffer
        val ySize = yPlane.remaining()
        val uSize = uPlane.remaining()
        val vSize = vPlane.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.get(nv21, 0, ySize)
        var pos = ySize

        for (row in 0 until image.height / 2) {
            for (col in 0 until image.width / 2) {
                val vIndex = row * image.planes[1].rowStride + col * image.planes[1].pixelStride
                val uIndex = row * image.planes[1].rowStride + col * image.planes[1].pixelStride
                nv21[pos++] = vPlane[vIndex]
                nv21[pos++] = uPlane[uIndex]
            }
        }
        return nv21
    }
}
