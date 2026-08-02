package com.medtrack.mobile.data.worker

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.NetworkUnavailableException
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.PendingScan
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.usecase.ProcessOfflineScanQueueUseCase
import com.medtrack.mobile.utils.notifications.OfflineScanNotifier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanUploadTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun successNotifiesCompletesAndDeletesExactlyOnce() = runBlocking {
        val repository = WorkerScanRepository(result = medication())
        val notifier = RecordingNotifier()
        val worker = worker(repository, notifier = notifier)

        assertResult("Success", worker.doWork())
        assertEquals(1, notifier.calls)
        assertEquals(listOf(1), repository.completed)
        assertEquals(listOf(1), repository.deleted)
    }

    @Test
    fun transientFailureRequestsRetry() = runBlocking {
        val repository = WorkerScanRepository(error = NetworkUnavailableException())
        assertResult("Retry", worker(repository).doWork())
        assertEquals(listOf(1), repository.retried)
    }

    @Test
    fun missingSessionFailsWithoutRetry() = runBlocking {
        val repository = WorkerScanRepository(error = InvalidSessionException())
        assertResult("Failure", worker(repository).doWork())
    }

    @Test
    fun finalUnexpectedFailureDoesNotRetryForever() = runBlocking {
        val repository = WorkerScanRepository(result = medication())
        val notifier = RecordingNotifier(failure = IllegalStateException("notification unavailable"))
        assertResult("Failure", worker(repository, notifier, runAttemptCount = 4).doWork())
    }

    private fun worker(
        repository: WorkerScanRepository,
        notifier: OfflineScanNotifier = RecordingNotifier(),
        runAttemptCount: Int = 0,
    ): ScanUpload {
        val factory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): ListenableWorker = ScanUpload(
                appContext,
                workerParameters,
                ProcessOfflineScanQueueUseCase(repository),
                repository,
                notifier,
                object : ScanFileCleanup {
                    override fun delete(image: ImageReference): Boolean = true
                },
            )
        }
        return TestListenableWorkerBuilder<ScanUpload>(context)
            .setWorkerFactory(factory)
            .setRunAttemptCount(runAttemptCount)
            .build()
    }

    private fun assertResult(expected: String, result: ListenableWorker.Result) {
        assertTrue("Expected $expected but was $result", result.toString().startsWith(expected))
    }

    private fun medication() = MedicamentoCapturadoDomain("Losartana", "Losartana", "50mg", "30", null)
}

private class RecordingNotifier(private val failure: Exception? = null) : OfflineScanNotifier {
    var calls = 0
    override fun show(scanId: Int, medicamento: MedicamentoCapturadoDomain) {
        failure?.let { throw it }
        calls++
    }
}

private class WorkerScanRepository(
    private val result: MedicamentoCapturadoDomain? = null,
    private val error: Exception? = null,
) : OfflineScanRepository {
    val completed = mutableListOf<Int>()
    val deleted = mutableListOf<Int>()
    val retried = mutableListOf<Int>()
    override suspend fun pendingScans() = listOf(PendingScan(1, ImageReference("scan.jpg"), "key", 0))
    override suspend fun completedScans(): List<PendingScan> = emptyList()
    override suspend fun claim(scanId: Int): Boolean = true
    override suspend fun uploadPending(scan: PendingScan): MedicamentoCapturadoDomain? {
        error?.let { throw it }
        return result
    }
    override suspend fun markUploaded(scanId: Int) = Unit
    override suspend fun markRetry(scanId: Int, reason: String) {
        retried += scanId
    }
    override suspend fun markFailed(scanId: Int, reason: String) = Unit
    override suspend fun markCompleted(scanId: Int) {
        completed += scanId
    }
    override suspend fun deleteCompleted(scanId: Int): Boolean {
        deleted += scanId
        return true
    }
    override suspend fun recoverInterrupted() = Unit
}
