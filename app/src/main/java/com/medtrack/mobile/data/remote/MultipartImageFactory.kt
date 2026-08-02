package com.medtrack.mobile.data.remote

import android.content.Context
import android.net.Uri
import com.medtrack.mobile.utils.MultipartImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import okhttp3.MultipartBody

interface ConfirmationImageSource {
    fun jpeg(uri: String?, filename: String): MultipartBody.Part?
}

class MultipartImageFactory @Inject constructor(@param:ApplicationContext private val context: Context) :
    ConfirmationImageSource {
    override fun jpeg(uri: String?, filename: String): MultipartBody.Part? = MultipartImageUtils.createJpegPart(
        context = context,
        uri = uri?.let(Uri::parse),
        partName = "imagem",
        filename = filename,
    )
}
