package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.NetworkUnavailableException
import com.medtrack.mobile.domain.error.RemoteRequestRejectedException
import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.PendingScan
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessOfflineScanQueueUseCaseTest {
    @Test
    fun `successful scan is claimed and marked uploaded`() = runTest {
        val repository = FakeOfflineScanRepository(result = medication())
        val result = ProcessOfflineScanQueueUseCase(repository)()
        assertFalse(result.shouldRetry)
        assertEquals(listOf(1), repository.uploadedIds)
        assertEquals(1, result.uploaded.size)
    }

    @Test
    fun `network failure is retryable`() = runTest {
        val repository = FakeOfflineScanRepository(error = NetworkUnavailableException())
        val result = ProcessOfflineScanQueueUseCase(repository)()
        assertTrue(result.shouldRetry)
        assertEquals(listOf(1), repository.retryIds)
    }

    @Test
    fun `null detection and rejected request are permanent failures`() = runTest {
        val nullRepository = FakeOfflineScanRepository(result = null)
        val rejectedRepository = FakeOfflineScanRepository(error = RemoteRequestRejectedException(422))
        assertEquals(1, ProcessOfflineScanQueueUseCase(nullRepository)().permanentFailures)
        assertEquals(1, ProcessOfflineScanQueueUseCase(rejectedRepository)().permanentFailures)
        assertFalse(ProcessOfflineScanQueueUseCase(rejectedRepository)().shouldRetry)
    }

    @Test
    fun `attempt limit fails without another upload`() = runTest {
        val repository = FakeOfflineScanRepository(attemptCount = ProcessOfflineScanQueueUseCase.MAX_ATTEMPTS - 1)
        val result = ProcessOfflineScanQueueUseCase(repository)()
        assertEquals(1, result.permanentFailures)
        assertEquals(0, repository.uploadCalls)
    }

    @Test
    fun `invalid session is propagated without retry loop`() {
        assertThrows(InvalidSessionException::class.java) {
            runTest {
                ProcessOfflineScanQueueUseCase(
                    FakeOfflineScanRepository(error = InvalidSessionException()),
                )()
            }
        }
    }

    private fun medication() = MedicamentoCapturadoDomain(
        nome = "Losartana",
        compostoAtivo = "Losartana Potassica",
        dosagem = "50mg",
        quantidade = "30 comprimidos",
        validade = "2027-01",
    )
}

private class FakeOfflineScanRepository(
    private val result: MedicamentoCapturadoDomain? = null,
    private val error: Exception? = null,
    attemptCount: Int = 0,
) : OfflineScanRepository {
    val uploadedIds = mutableListOf<Int>()
    val retryIds = mutableListOf<Int>()
    var uploadCalls = 0
    private val pending = PendingScan(1, ImageReference("file:///tmp/scan.jpg"), "key", attemptCount)

    override suspend fun pendingScans(): List<PendingScan> = listOf(pending)
    override suspend fun completedScans(): List<PendingScan> = emptyList()
    override suspend fun claim(scanId: Int): Boolean = true
    override suspend fun uploadPending(scan: PendingScan): MedicamentoCapturadoDomain? {
        uploadCalls++
        error?.let { throw it }
        return result
    }
    override suspend fun markUploaded(scanId: Int) {
        uploadedIds += scanId
    }
    override suspend fun markRetry(scanId: Int, reason: String) {
        retryIds += scanId
    }
    override suspend fun markFailed(scanId: Int, reason: String) = Unit
    override suspend fun markCompleted(scanId: Int) = Unit
    override suspend fun deleteCompleted(scanId: Int): Boolean = true
    override suspend fun recoverInterrupted() = Unit
}
