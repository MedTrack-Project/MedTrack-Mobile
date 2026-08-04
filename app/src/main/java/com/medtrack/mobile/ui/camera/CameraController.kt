package com.medtrack.mobile.ui.camera

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface CameraController {
    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner)

    fun capturePhotoOnly(onImageCaptured: (Uri?) -> Unit)
}
