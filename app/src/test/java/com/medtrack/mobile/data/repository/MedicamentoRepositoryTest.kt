package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.data.local.entity.FrequenciaUsoEntity
import com.medtrack.mobile.data.local.entity.MedicamentoEntity
import com.medtrack.mobile.data.local.entity.UsuarioEntity
import com.medtrack.mobile.data.local.source.MedicationLocalSource
import com.medtrack.mobile.data.remote.ConfirmationImageSource
import com.medtrack.mobile.data.remote.dto.ConfirmacaoRequestDto
import com.medtrack.mobile.data.remote.dto.FrequenciaUsoDto
import com.medtrack.mobile.data.remote.dto.MedicamentoDto
import com.medtrack.mobile.data.remote.dto.UsuarioDto
import com.medtrack.mobile.data.remote.source.MedicationRemoteSource
import com.medtrack.mobile.domain.error.ConfirmationAlreadyExistsException
import com.medtrack.mobile.domain.error.DoseOutsideAllowedTimeException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.service.MedicationScheduler
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MedicamentoRepositoryTest {
    @Test
    fun `successful synchronization replaces snapshot and schedules cached medications`() = runTest {
        val local = MutableMedicationLocalSource()
        val scheduler = RecordingMedicationScheduler()
        val repository = repository(
            remote = RecordingMedicationRemoteSource(
                remoteUser = UsuarioDto(1, "Yann", "yann@example.test", "yann"),
                remoteMedications = listOf(remoteMedication()),
            ),
            local = local,
            scheduler = scheduler,
        )

        val result = repository.synchronizeUserData("token")

        assertEquals("token", result.token)
        assertEquals("Losartana", result.medicamentos.single().nome)
        assertEquals(1, local.snapshotWrites)
        assertEquals(listOf(1L), scheduler.scheduled.map(MedicamentoDomain::id))
    }

    @Test
    fun `confirmed dose keys include only synchronized confirmations`() = runTest {
        val local = MutableMedicationLocalSource().apply {
            confirmations += confirmation(synchronized = true)
            confirmations += confirmation(id = 2, time = "20:00:00", synchronized = false)
        }

        val keys = repository(local = local).confirmedDoseKeys()

        assertEquals(setOf("1_2026-08-03_08:00"), keys)
    }

    @Test
    fun `selected future dose is rejected before remote mutation`() {
        val local = localWithMedication()
        val remote = RecordingMedicationRemoteSource()
        val repository = repository(remote = remote, local = local)

        assertThrows(DoseOutsideAllowedTimeException::class.java) {
            runTest {
                repository.confirmMedication(
                    confirmationCommand(date = "2026-08-03", time = "13:00", selectedId = 1),
                )
            }
        }
        assertEquals(0, remote.confirmations.size)
        assertEquals(0, local.savedConfirmations.size)
    }

    @Test
    fun `already synchronized dose is rejected without duplicate request`() {
        val local = localWithMedication().apply {
            confirmations += confirmation(synchronized = true)
        }
        val remote = RecordingMedicationRemoteSource()
        val repository = repository(remote = remote, local = local)

        assertThrows(ConfirmationAlreadyExistsException::class.java) {
            runTest {
                repository.confirmMedication(
                    confirmationCommand(date = "2026-08-03", time = "08:00", selectedId = 1),
                )
            }
        }
        assertEquals(0, remote.confirmations.size)
    }

    @Test
    fun `valid confirmation persists only after successful remote request`() = runTest {
        val local = localWithMedication()
        val remote = RecordingMedicationRemoteSource()
        val repository = repository(remote = remote, local = local)

        repository.confirmMedication(
            confirmationCommand(date = "2026-08-03", time = "08:00:00", selectedId = 1),
        )

        assertEquals("08:00", remote.confirmations.single().horario)
        assertEquals(true, local.savedConfirmations.single().sincronizado)
        assertEquals(true, local.savedConfirmations.single().foiTomado)
    }

    private fun repository(
        remote: RecordingMedicationRemoteSource = RecordingMedicationRemoteSource(),
        local: MutableMedicationLocalSource = MutableMedicationLocalSource(),
        scheduler: RecordingMedicationScheduler = RecordingMedicationScheduler(),
    ) = MedicamentoRepository(
        remote = remote,
        local = local,
        images = NoConfirmationImageSource,
        notificationScheduler = scheduler,
        clock = FixedRepositoryClock,
        dispatchers = RepositoryTestDispatchers,
    )
}

private class RecordingMedicationRemoteSource(
    private val remoteUser: UsuarioDto = UsuarioDto(1, "Yann", "yann@example.test", "yann"),
    private val remoteMedications: List<MedicamentoDto> = emptyList(),
) : MedicationRemoteSource {
    val confirmations = mutableListOf<ConfirmacaoRequestDto>()

    override suspend fun user(): UsuarioDto = remoteUser
    override suspend fun medications(): List<MedicamentoDto> = remoteMedications
    override suspend fun confirm(request: ConfirmacaoRequestDto, image: MultipartBody.Part?) {
        confirmations += request
    }
}

private class MutableMedicationLocalSource : MedicationLocalSource {
    var userEntity = UsuarioEntity(1, "Yann", "yann@example.test", "yann")
    val medicationItems = mutableListOf<MedicamentoEntity>()
    val confirmations = mutableListOf<ConfirmacaoEntity>()
    val savedConfirmations = mutableListOf<ConfirmacaoEntity>()
    var snapshotWrites = 0

    override suspend fun replaceUserSnapshot(user: UsuarioEntity, medicationItems: List<MedicamentoEntity>) {
        snapshotWrites++
        userEntity = user
        this.medicationItems.clear()
        this.medicationItems += medicationItems
    }

    override suspend fun user(): UsuarioEntity = userEntity
    override suspend fun medication(id: Long): MedicamentoEntity? = medicationItems.firstOrNull { it.id == id }
    override suspend fun medications(): List<MedicamentoEntity> = medicationItems
    override suspend fun confirmations(): List<ConfirmacaoEntity> = confirmations

    override suspend fun confirmation(medicationId: Long, date: String, time: String): ConfirmacaoEntity? =
        confirmations.firstOrNull {
            it.medicamentoId == medicationId && it.data == date && it.horario.take(5) == time.take(5)
        }

    override suspend fun saveConfirmation(value: ConfirmacaoEntity) {
        savedConfirmations += value
    }
}

private class RecordingMedicationScheduler : MedicationScheduler {
    val scheduled = mutableListOf<MedicamentoDomain>()
    override suspend fun schedule(medicamento: MedicamentoDomain) {
        scheduled += medicamento
    }
}

private object NoConfirmationImageSource : ConfirmationImageSource {
    override fun jpeg(uri: String?, filename: String): MultipartBody.Part? = null
}

private fun localWithMedication() = MutableMedicationLocalSource().apply {
    medicationItems += medicationEntity()
}

private fun medicationEntity() = MedicamentoEntity(
    id = 1,
    nome = "Losartana",
    compostoAtivo = "Losartana Potassica",
    dosagem = "50mg",
    imagemUrl = null,
    frequenciaUso = FrequenciaUsoEntity(
        frequenciaUsoTipo = "HORARIOS_ESPECIFICOS",
        usoContinuo = true,
        horariosEspecificos = "[\"08:00\",\"20:00\"]",
        intervaloHoras = null,
        primeiroHorario = null,
        dataInicio = "2026-08-01",
        dataTermino = null,
    ),
)

private fun remoteMedication() = MedicamentoDto(
    id = 1,
    nome = "Losartana",
    compostoAtivo = "Losartana Potassica",
    dosagem = "50mg",
    frequenciaUso = FrequenciaUsoDto(
        frequenciaUsoTipo = "HORARIOS_ESPECIFICOS",
        usoContinuo = true,
        horariosEspecificos = listOf("08:00", "20:00"),
        dataInicio = "2026-08-01",
    ),
)

private fun confirmation(id: Long = 1, time: String = "08:00:00", synchronized: Boolean) = ConfirmacaoEntity(
    id = id,
    medicamentoId = 1,
    horario = time,
    data = LocalDate.of(2026, 8, 3).toString(),
    foiTomado = true,
    sincronizado = synchronized,
)

private fun confirmationCommand(date: String, time: String, selectedId: Long?) = ConfirmationCommand(
    medicamentoCapturado = MedicamentoCapturadoDomain(
        nome = "Losartana",
        compostoAtivo = "Losartana Potassica",
        dosagem = "50mg",
        quantidade = "30",
        validade = "2027-01",
    ),
    comprovanteImagem = null,
    medicamentoSelecionadoId = selectedId,
    dataSelecionada = date,
    horarioSelecionado = time,
)
