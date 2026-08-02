package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.data.local.entity.MedicamentoEntity
import com.medtrack.mobile.data.local.entity.UsuarioEntity
import com.medtrack.mobile.data.local.source.MedicationLocalSource
import com.medtrack.mobile.data.remote.ConfirmationImageSource
import com.medtrack.mobile.data.remote.dto.ConfirmacaoRequestDto
import com.medtrack.mobile.data.remote.dto.MedicamentoDto
import com.medtrack.mobile.data.remote.dto.UsuarioDto
import com.medtrack.mobile.data.remote.source.MedicationRemoteSource
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.error.ServerUnavailableException
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.service.MedicationScheduler
import com.medtrack.mobile.domain.time.AppClock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationRepositoryConsistencyTest {
    @Test
    fun `failed remote snapshot does not mutate local source of truth`() {
        val local = RecordingLocalSource()
        val repository = MedicamentoRepository(
            remote = FailingSnapshotRemoteSource(),
            local = local,
            images = object : ConfirmationImageSource {
                override fun jpeg(uri: String?, filename: String): MultipartBody.Part? = null
            },
            notificationScheduler = object : MedicationScheduler {
                override suspend fun schedule(medicamento: MedicamentoDomain) = Unit
            },
            clock = FixedClock,
            dispatchers = TestDispatchers,
        )

        assertThrows(ServerUnavailableException::class.java) {
            runTest { repository.synchronizeUserData("token") }
        }
        assertEquals(0, local.snapshotWrites)
    }
}

private class FailingSnapshotRemoteSource : MedicationRemoteSource {
    override suspend fun user() = UsuarioDto(1, "Yann", "yann@example.test", "yann")
    override suspend fun medications(): List<MedicamentoDto> = throw ServerUnavailableException()
    override suspend fun confirm(request: ConfirmacaoRequestDto, image: MultipartBody.Part?) = Unit
}

private class RecordingLocalSource : MedicationLocalSource {
    var snapshotWrites = 0
    override suspend fun replaceUserSnapshot(user: UsuarioEntity, medicationItems: List<MedicamentoEntity>) {
        snapshotWrites++
    }
    override suspend fun user(): UsuarioEntity = error("Nao usado")
    override suspend fun medication(id: Long): MedicamentoEntity? = null
    override suspend fun medications(): List<MedicamentoEntity> = emptyList()
    override suspend fun confirmations(): List<ConfirmacaoEntity> = emptyList()
    override suspend fun confirmation(medicationId: Long, date: String, time: String): ConfirmacaoEntity? = null
    override suspend fun saveConfirmation(value: ConfirmacaoEntity) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
private object TestDispatchers : DispatcherProvider {
    override val io: CoroutineDispatcher = UnconfinedTestDispatcher()
    override val default: CoroutineDispatcher = io
}

private object FixedClock : AppClock {
    override fun instant(): Instant = Instant.EPOCH
    override fun localDate(): LocalDate = LocalDate.of(2026, 8, 2)
    override fun localTime(): LocalTime = LocalTime.NOON
    override fun localDateTime(): LocalDateTime = LocalDateTime.of(localDate(), localTime())
    override fun zoneId(): ZoneId = ZoneId.of("UTC")
}
