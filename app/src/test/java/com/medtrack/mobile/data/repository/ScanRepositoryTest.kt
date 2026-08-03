package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.local.daos.ScanQueueDao
import com.medtrack.mobile.data.local.entity.ScanQueueItem
import com.medtrack.mobile.data.local.entity.ScanQueueStatus
import com.medtrack.mobile.data.remote.source.ScanRemoteSource
import com.medtrack.mobile.data.worker.OfflineScanWorkScheduler
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.time.AppClock
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ScanRepositoryTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test
    fun `enqueue hashes file content and schedules unique processing`() = runTest {
        val file = temporaryFolder.newFile("scan.jpg").apply { writeText("fixture-content") }
        val dao = RecordingScanQueueDao()
        val scheduler = RecordingOfflineScanScheduler()
        val repository = repository(dao = dao, scheduler = scheduler)

        repository.enqueue(ImageReference(file.toURI().toString()))

        assertEquals(
            "2b0651992ab2137dafbfbf37f6d5358419c0917b9833465a89bba63b49e2fe51",
            dao.inserted.single().idempotencyKey,
        )
        assertEquals(FixedRepositoryClock.instant().toEpochMilli(), dao.inserted.single().timestamp)
        assertEquals(1, scheduler.requests)
    }

    @Test
    fun `queue operations preserve state attempts and stale threshold`() = runTest {
        val dao = RecordingScanQueueDao().apply {
            processable += queueItem(id = 7, status = ScanQueueStatus.RETRY, attemptCount = 2)
            completed += queueItem(id = 8, status = ScanQueueStatus.COMPLETED)
            claimResult = 1
            deleteResult = 0
        }
        val repository = repository(dao = dao)

        assertEquals(2, repository.pendingScans().single().attemptCount)
        assertEquals(8, repository.completedScans().single().id)
        assertTrue(repository.claim(7))
        repository.markUploaded(7)
        repository.markRetry(7, "network")
        repository.markFailed(7, "invalid")
        repository.markCompleted(7)
        assertFalse(repository.deleteCompleted(7))
        repository.recoverInterrupted()

        assertEquals(
            listOf(
                ScanQueueStatus.UPLOADED,
                ScanQueueStatus.RETRY,
                ScanQueueStatus.FAILED,
                ScanQueueStatus.COMPLETED,
            ),
            dao.updates.map { it.status },
        )
        assertEquals(FixedRepositoryClock.instant().toEpochMilli() - 15 * 60 * 1_000L, dao.staleBefore)
    }

    @Test
    fun `scan and pending upload delegate the resolved file to remote source`() = runTest {
        val file = temporaryFolder.newFile("remote.jpg")
        val remote = RecordingScanRemoteSource()
        val repository = repository(remote = remote)

        repository.scan(ImageReference(file.absolutePath))
        repository.uploadPending(
            com.medtrack.mobile.domain.model.PendingScan(
                id = 1,
                image = ImageReference(file.toURI().toString()),
                idempotencyKey = "key",
                attemptCount = 0,
            ),
        )

        assertEquals(listOf(file.absolutePath, file.absolutePath), remote.files.map(File::getAbsolutePath))
    }

    private fun repository(
        remote: ScanRemoteSource = RecordingScanRemoteSource(),
        dao: RecordingScanQueueDao = RecordingScanQueueDao(),
        scheduler: OfflineScanWorkScheduler = RecordingOfflineScanScheduler(),
    ) = ScanRepository(remote, dao, scheduler, FixedRepositoryClock, RepositoryTestDispatchers)
}

private data class QueueUpdate(val id: Int, val status: ScanQueueStatus, val error: String?)

private class RecordingScanQueueDao : ScanQueueDao {
    val inserted = mutableListOf<ScanQueueItem>()
    val processable = mutableListOf<ScanQueueItem>()
    val completed = mutableListOf<ScanQueueItem>()
    val updates = mutableListOf<QueueUpdate>()
    var claimResult = 0
    var deleteResult = 0
    var staleBefore: Long? = null

    override suspend fun insert(item: ScanQueueItem): Long {
        inserted += item
        return inserted.size.toLong()
    }

    override suspend fun getProcessableScans(): List<ScanQueueItem> = processable
    override suspend fun getCompletedScans(): List<ScanQueueItem> = completed
    override suspend fun claim(id: Int, updatedAt: Long): Int = claimResult

    override suspend fun updateState(id: Int, status: ScanQueueStatus, updatedAt: Long, lastError: String?) {
        updates += QueueUpdate(id, status, lastError)
    }

    override suspend fun getById(id: Int): ScanQueueItem? = (processable + completed).firstOrNull { it.id == id }
    override suspend fun deleteCompleted(id: Int): Int = deleteResult

    override suspend fun recoverStaleProcessing(staleBefore: Long, now: Long): Int {
        this.staleBefore = staleBefore
        return 1
    }
}

private class RecordingOfflineScanScheduler : OfflineScanWorkScheduler {
    var requests = 0
    override fun enqueue() {
        requests++
    }
}

private class RecordingScanRemoteSource : ScanRemoteSource {
    val files = mutableListOf<File>()
    override suspend fun scan(file: File): MedicamentoCapturadoDomain? {
        files += file
        return null
    }
}

internal object FixedRepositoryClock : AppClock {
    override fun instant(): Instant = Instant.parse("2026-08-03T12:00:00Z")
    override fun localDate(): LocalDate = LocalDate.of(2026, 8, 3)
    override fun localTime(): LocalTime = LocalTime.NOON
    override fun localDateTime(): LocalDateTime = LocalDateTime.of(localDate(), localTime())
    override fun zoneId(): ZoneId = ZoneId.of("UTC")
}

private fun queueItem(id: Int, status: ScanQueueStatus, attemptCount: Int = 0) = ScanQueueItem(
    id = id,
    imagePath = "/tmp/$id.jpg",
    idempotencyKey = "key-$id",
    status = status,
    timestamp = 1L,
    attemptCount = attemptCount,
    updatedAt = 1L,
)
