package com.example.piec_1.data.repository

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.piec_1.core.config.ApiEndpoints
import com.example.piec_1.data.local.AppDatabase
import com.example.piec_1.data.local.entity.ScanQueueItem
import com.example.piec_1.data.remote.ApiService
import com.example.piec_1.data.remote.dto.ScanResponseDto
import com.example.piec_1.data.remote.mapper.toCapturadoDomain
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.domain.service.ScanUpload
import com.example.piec_1.utils.exceptions.TokenNaoEncontradoException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class ScanRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiService: ApiService,
    database: AppDatabase,
    private val authRepository: AuthRepository,
    private val endpoints: ApiEndpoints,
) {
    private val scanQueueDao = database.scanQueueDao()

    suspend fun scanMedicamento(file: File): MedicamentoCapturadoDomain? = withContext(Dispatchers.IO) {
        val token = authRepository.getToken() ?: throw TokenNaoEncontradoException()
        enviarImagemParaScan(file, token, "file")?.data?.toCapturadoDomain()
    }

    suspend fun getPendingScans(): List<ScanQueueItem> = withContext(Dispatchers.IO) {
        scanQueueDao.getPendingScans()
    }

    suspend fun updateScanStatus(id: Int, status: String) = withContext(Dispatchers.IO) {
        scanQueueDao.updateStatus(id, status)
    }

    suspend fun uploadScanPendente(file: File): MedicamentoCapturadoDomain? = withContext(Dispatchers.IO) {
        val token = authRepository.getToken() ?: throw TokenNaoEncontradoException()
        val partNames = listOf("file", "image", "photo")

        for (partName in partNames) {
            val response = enviarImagemParaScan(file, token, partName)
            if (response?.data != null) {
                return@withContext response.data.toCapturadoDomain()
            }
        }

        null
    }

    suspend fun salvarScanOffline(uri: Uri) = withContext(Dispatchers.IO) {
        scanQueueDao.insert(
            ScanQueueItem(
                imagePath = uri.toString(),
                status = "PENDENTE",
                timestamp = System.currentTimeMillis(),
            ),
        )
        agendarProcessamentoDeScansOffline()
    }

    private fun agendarProcessamentoDeScansOffline() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val scanWorkRequest = OneTimeWorkRequestBuilder<ScanUpload>()
            .setConstraints(constraints)
            .addTag("offline_scan_job")
            .build()

        WorkManager.getInstance(context).enqueue(scanWorkRequest)
    }

    private suspend fun enviarImagemParaScan(file: File, token: String, partName: String): ScanResponseDto? {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData(partName, file.name, requestFile)
        val response = apiService.scanMedicamento(endpoints.scanUrl, "Bearer $token", body)

        return if (response.isSuccessful) response.body() else null
    }
}
