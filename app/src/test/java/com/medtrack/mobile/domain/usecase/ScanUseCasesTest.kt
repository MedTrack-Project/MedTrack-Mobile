package com.medtrack.mobile.domain.usecase

import com.medtrack.mobile.domain.model.ImageReference
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.repository.ScanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanUseCasesTest {
    @Test
    fun `scan delegates image reference and returns medication`() = runTest {
        val repository = FakeScanRepository(medicamento())
        val image = ImageReference("file:///tmp/scan.jpg")

        val result = ScanMedicationUseCase(repository)(image)

        assertEquals(image, repository.scannedImage)
        assertEquals(medicamento(), result)
    }

    @Test
    fun `offline queue delegates image reference`() = runTest {
        val repository = FakeScanRepository(null)
        val image = ImageReference("file:///tmp/offline.jpg")

        QueueOfflineScanUseCase(repository)(image)

        assertEquals(image, repository.queuedImage)
    }

    private fun medicamento() = MedicamentoCapturadoDomain(
        nome = "Losartana",
        compostoAtivo = "Losartana Potassica",
        dosagem = "50mg",
        quantidade = "30 comprimidos",
        validade = "2027-01",
    )
}

private class FakeScanRepository(private val result: MedicamentoCapturadoDomain?) : ScanRepository {
    var scannedImage: ImageReference? = null
    var queuedImage: ImageReference? = null

    override suspend fun scan(image: ImageReference): MedicamentoCapturadoDomain? {
        scannedImage = image
        return result
    }

    override suspend fun enqueue(image: ImageReference) {
        queuedImage = image
    }
}
