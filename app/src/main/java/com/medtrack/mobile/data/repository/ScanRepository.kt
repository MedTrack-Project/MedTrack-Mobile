package com.medtrack.mobile.data.repository

import android.net.Uri
import com.medtrack.mobile.data.local.daos.ScanQueueDao
import com.medtrack.mobile.data.local.entity.ScanQueueItem
import com.medtrack.mobile.data.local.entity.ScanQueueStatus
import com.medtrack.mobile.data.remote.source.ScanRemoteSource
import com.medtrack.mobile.data.worker.OfflineScanWorkScheduler
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.PendingScan
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.time.AppClock
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class ScanRepository @Inject constructor(
    private val remote: ScanRemoteSource,
    private val scanQueueDao: ScanQueueDao,
    private val workScheduler: OfflineScanWorkScheduler,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : com.medtrack.mobile.domain.repository.ScanRepository,
    OfflineScanRepository {

    override suspend fun scan(image: ImageReference): MedicamentoCapturadoDomain? = withContext(dispatchers.io) {
        val file = image.asFile()
        remote.scan(file)
    }

    override suspend fun pendingScans(): List<PendingScan> = withContext(dispatchers.io) {
        scanQueueDao.getProcessableScans().map { it.toDomain() }
    }

    override suspend fun completedScans(): List<PendingScan> = withContext(dispatchers.io) {
        scanQueueDao.getCompletedScans().map { it.toDomain() }
    }

    override suspend fun claim(scanId: Int): Boolean = withContext(dispatchers.io) {
        scanQueueDao.claim(scanId, clock.instant().toEpochMilli()) == 1
    }

    override suspend fun markUploaded(scanId: Int) = updateState(scanId, ScanQueueStatus.UPLOADED)

    override suspend fun markRetry(scanId: Int, reason: String) = updateState(scanId, ScanQueueStatus.RETRY, reason)

    override suspend fun markFailed(scanId: Int, reason: String) = updateState(scanId, ScanQueueStatus.FAILED, reason)

    override suspend fun markCompleted(scanId: Int) = updateState(scanId, ScanQueueStatus.COMPLETED)

    override suspend fun deleteCompleted(scanId: Int): Boolean = withContext(dispatchers.io) {
        scanQueueDao.deleteCompleted(scanId) == 1
    }

    override suspend fun recoverInterrupted() = withContext(dispatchers.io) {
        val now = clock.instant().toEpochMilli()
        scanQueueDao.recoverStaleProcessing(now - STALE_PROCESSING_MILLIS, now)
        Unit
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
                idempotencyKey = image.idempotencyKey(),
                timestamp = clock.instant().toEpochMilli(),
            ),
        )
        workScheduler.enqueue()
    }

    private suspend fun updateState(scanId: Int, status: ScanQueueStatus, reason: String? = null) {
        withContext(dispatchers.io) {
            scanQueueDao.updateState(scanId, status, clock.instant().toEpochMilli(), reason)
        }
    }

    private fun ImageReference.asFile(): File = Uri.parse(value).path?.let(::File) ?: File(value)

    private fun ImageReference.idempotencyKey(): String {
        val file = asFile()
        val digest = MessageDigest.getInstance("SHA-256")
        if (file.isFile) {
            file.inputStream().buffered().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                }
            }
        } else {
            digest.update(value.toByteArray())
        }
        return digest.digest()
            .joinToString("") { "%02x".format(it) }
    }

    private fun ScanQueueItem.toDomain() = PendingScan(
        id = id,
        image = ImageReference(imagePath),
        idempotencyKey = idempotencyKey,
        attemptCount = attemptCount,
    )

    private companion object {
        const val STALE_PROCESSING_MILLIS = 15 * 60 * 1_000L
    }
}
