package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.error.InvalidSessionException
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
    fun `marks successful scans as completed`() = runTest {
        val repository = FakeOfflineScanRepository(result = medicamento())

        val result = ProcessOfflineScanQueueUseCase(repository)()

        assertFalse(result.shouldRetry)
        assertEquals(listOf(1), repository.completedIds)
        assertEquals(1, result.completed.size)
    }

    @Test
    fun `requests retry when scan cannot be processed`() = runTest {
        val result = ProcessOfflineScanQueueUseCase(FakeOfflineScanRepository(result = null))()

        assertTrue(result.shouldRetry)
        assertTrue(result.completed.isEmpty())
    }

    @Test
    fun `propagates invalid session without retry loop`() {
        assertThrows(InvalidSessionException::class.java) {
            kotlinx.coroutines.test.runTest {
                ProcessOfflineScanQueueUseCase(
                    FakeOfflineScanRepository(error = InvalidSessionException()),
                )()
            }
        }
    }

    private fun medicamento() = MedicamentoCapturadoDomain(
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
) : OfflineScanRepository {
    val completedIds = mutableListOf<Int>()
    private val pending = PendingScan(1, ImageReference("file:///tmp/scan.jpg"))

    override suspend fun pendingScans(): List<PendingScan> = listOf(pending)

    override suspend fun uploadPending(scan: PendingScan): MedicamentoCapturadoDomain? {
        error?.let { throw it }
        return result
    }

    override suspend fun markCompleted(scanId: Int) {
        completedIds += scanId
    }
}
