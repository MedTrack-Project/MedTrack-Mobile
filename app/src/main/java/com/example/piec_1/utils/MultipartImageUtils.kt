package com.example.piec_1.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

object MultipartImageUtils {
    fun createJpegPart(context: Context, uri: Uri?, partName: String, filename: String): MultipartBody.Part? {
        if (uri == null || !canOpen(context, uri)) return null

        val requestBody = runCatching {
            uri.asJpegRequestBody(context)
        }.getOrNull() ?: return null

        return MultipartBody.Part.createFormData(
            name = partName,
            filename = filename,
            body = requestBody,
        )
    }

    fun canOpen(context: Context, uri: Uri): Boolean = runCatching {
        context.contentResolver.openInputStream(uri)?.use { true } ?: false
    }.getOrDefault(false) ||
        uri.path?.let { File(it).exists() } == true

    private fun Uri.asJpegRequestBody(context: Context): RequestBody {
        val imageMediaType = "image/jpeg".toMediaType()

        return object : RequestBody() {
            override fun contentType() = imageMediaType

            override fun writeTo(sink: BufferedSink) {
                val inputStream = runCatching {
                    context.contentResolver.openInputStream(this@asJpegRequestBody)
                }.getOrNull()
                    ?: path?.let { FileInputStream(File(it)) }
                    ?: throw IOException("Imagem indisponivel")

                inputStream.use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }
}
