package com.medtrack.mobile.data.repository

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.medtrack.mobile.data.local.daos.ScanQueueDao
import com.medtrack.mobile.data.local.entity.ScanQueueItem
import com.medtrack.mobile.data.remote.source.ScanRemoteSource
import com.medtrack.mobile.data.worker.ScanUpload
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.PendingScan
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.time.AppClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class ScanRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val remote: ScanRemoteSource,
    private val scanQueueDao: ScanQueueDao,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : com.medtrack.mobile.domain.repository.ScanRepository,
    OfflineScanRepository {

    override suspend fun scan(image: ImageReference): MedicamentoCapturadoDomain? = withContext(dispatchers.io) {
        val file = image.asFile()
        remote.scan(file)
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
        remote.scan(file)
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

    private fun ImageReference.asFile(): File = Uri.parse(value).path?.let(::File) ?: File(value)
}
