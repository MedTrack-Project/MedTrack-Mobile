package com.medtrack.mobile.data.repository

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.medtrack.mobile.core.config.ApiEndpoints
import com.medtrack.mobile.data.local.daos.ScanQueueDao
import com.medtrack.mobile.data.local.entity.ScanQueueItem
import com.medtrack.mobile.data.mapper.remote.toCapturadoDomain
import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.dto.ScanResponseDto
import com.medtrack.mobile.data.worker.ScanUpload
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.ScanProcessingException
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.PendingScan
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.repository.SessionRepository
import com.medtrack.mobile.domain.time.AppClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class ScanRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val scanQueueDao: ScanQueueDao,
    private val sessionRepository: SessionRepository,
    private val endpoints: ApiEndpoints,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : com.medtrack.mobile.domain.repository.ScanRepository,
    OfflineScanRepository {

    override suspend fun scan(image: ImageReference): MedicamentoCapturadoDomain? = withContext(dispatchers.io) {
        val file = image.asFile()
        val token = sessionRepository.getToken() ?: throw InvalidSessionException()
        enviarImagemParaScan(file, token, "file")?.data?.toCapturadoDomain()
    }

    override suspend fun pendingScans(): List<PendingScan> = withContext(dispatchers.io) {
        scanQueueDao.getPendingScans().map {
            PendingScan(id = it.id, image = ImageReference(it.imagePath))
        }
    }

    override suspend fun markCompleted(scanId: Int) = withContext(dispatchers.io) {
        scanQueueDao.updateStatus(scanId, "CONCLUIDO")
    }

    override suspend fun uploadPending(scan: PendingScan): MedicamentoCapturadoDomain? =
        uploadScanPendente(scan.image.asFile())

    suspend fun uploadScanPendente(file: File): MedicamentoCapturadoDomain? = withContext(dispatchers.io) {
        val token = sessionRepository.getToken() ?: throw InvalidSessionException()
        val partNames = listOf("file", "image", "photo")

        for (partName in partNames) {
            val response = enviarImagemParaScan(file, token, partName)
            if (response?.data != null) {
                return@withContext response.data.toCapturadoDomain()
            }
        }

        null
    }

    override suspend fun enqueue(image: ImageReference) = withContext(dispatchers.io) {
        scanQueueDao.insert(
            ScanQueueItem(
                imagePath = image.value,
                status = "PENDENTE",
                timestamp = clock.instant().toEpochMilli(),
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
        val response = runCatching {
            apiService.scanMedicamento(endpoints.scanUrl, "Bearer $token", body)
        }.getOrElse { throw ScanProcessingException(it) }

        return if (response.isSuccessful) response.body() else null
    }

    private fun ImageReference.asFile(): File = Uri.parse(value).path?.let(::File) ?: File(value)
}
