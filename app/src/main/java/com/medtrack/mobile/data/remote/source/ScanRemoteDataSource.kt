package com.medtrack.mobile.data.remote.source

import com.medtrack.mobile.core.config.ApiEndpoints
import com.medtrack.mobile.data.mapper.remote.toCapturadoDomain
import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.RemoteCallExecutor
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import java.io.File
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

interface ScanRemoteSource {
    suspend fun scan(file: File): MedicamentoCapturadoDomain?
}

class ScanRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val calls: RemoteCallExecutor,
    private val endpoints: ApiEndpoints,
) : ScanRemoteSource {
    override suspend fun scan(file: File): MedicamentoCapturadoDomain? {
        val image = MultipartBody.Part.createFormData(
            SCAN_PART_NAME,
            file.name,
            file.asRequestBody(JPEG_MEDIA_TYPE),
        )
        return calls.execute { api.scanMedicamento(endpoints.scanUrl, image) }
            .data
            ?.toCapturadoDomain()
    }

    companion object {
        const val SCAN_PART_NAME = "file"
        private val JPEG_MEDIA_TYPE = "image/jpeg".toMediaType()
    }
}
