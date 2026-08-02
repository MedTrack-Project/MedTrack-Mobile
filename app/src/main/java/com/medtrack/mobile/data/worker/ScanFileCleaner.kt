package com.medtrack.mobile.data.worker

import android.net.Uri
import com.medtrack.mobile.domain.model.ImageReference
import java.io.File
import javax.inject.Inject

interface ScanFileCleanup {
    fun delete(image: ImageReference): Boolean
}

class ScanFileCleaner @Inject constructor() : ScanFileCleanup {
    override fun delete(image: ImageReference): Boolean {
        val file = Uri.parse(image.value).path?.let(::File) ?: File(image.value)
        return !file.exists() || file.delete()
    }
}
