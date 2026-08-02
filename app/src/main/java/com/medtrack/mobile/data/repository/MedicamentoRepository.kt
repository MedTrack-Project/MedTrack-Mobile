package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.data.local.source.MedicationLocalSource
import com.medtrack.mobile.data.mapper.local.toDomain
import com.medtrack.mobile.data.mapper.local.toEntity
import com.medtrack.mobile.data.mapper.remote.toDomain
import com.medtrack.mobile.data.remote.ConfirmationImageSource
import com.medtrack.mobile.data.remote.dto.ConfirmacaoRequestDto
import com.medtrack.mobile.data.remote.source.MedicationRemoteSource
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.error.ConfirmationAlreadyExistsException
import com.medtrack.mobile.domain.error.DoseOutsideAllowedTimeException
import com.medtrack.mobile.domain.error.InvalidRemoteResponseException
import com.medtrack.mobile.domain.error.MedicationNotFoundException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.LoginResult
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.repository.MedicationRepository
import com.medtrack.mobile.domain.service.MedicationScheduler
import com.medtrack.mobile.domain.time.AppClock
import com.medtrack.mobile.domain.usecase.doseKey
import com.medtrack.mobile.domain.usecase.horariosDoDia
import com.medtrack.mobile.utils.MedicationTextMatcher
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class MedicamentoRepository @Inject constructor(
    private val remote: MedicationRemoteSource,
    private val local: MedicationLocalSource,
    private val images: ConfirmationImageSource,
    private val notificationScheduler: MedicationScheduler,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : MedicationRepository {
    override suspend fun synchronizeUserData(token: String): LoginResult = withContext(dispatchers.io) {
        val usuarioDto = remote.user()
        val medicamentoDtos = remote.medications()
        val usuario = mapRemote { usuarioDto.toDomain() }
        val medicamentos = mapRemote { medicamentoDtos.map { it.toDomain() } }

        local.replaceUserSnapshot(usuario.toEntity(), medicamentos.map { it.toEntity() })
        val cachedUser = local.user().toDomain()
        val cachedMedications = local.medications().map { it.toDomain() }
        cachedMedications.forEach { notificationScheduler.schedule(it) }

        LoginResult(
            token = token,
            usuario = cachedUser,
            medicamentos = cachedMedications,
        )
    }

    override suspend fun findMedication(medicamentoId: Long): MedicamentoDomain? = withContext(dispatchers.io) {
        local.medication(medicamentoId)?.toDomain()
    }

    override suspend fun confirmedDoseKeys(): Set<String> = withContext(dispatchers.io) {
        local.confirmations()
            .filter { it.sincronizado }
            .map { confirmacao ->
                doseKey(
                    medicamentoId = confirmacao.medicamentoId,
                    date = LocalDate.parse(confirmacao.data),
                    horario = confirmacao.horario.take(5),
                )
            }.toSet()
    }

    override suspend fun confirmMedication(command: ConfirmationCommand) = withContext(dispatchers.io) {
        val medicamentoCorrespondente = command.medicamentoSelecionadoId
            ?.let { local.medication(it)?.toDomain() }
            ?: encontrarMedicamentoCorrespondente(command.medicamentoCapturado)
            ?: throw MedicationNotFoundException()

        processarConfirmacao(
            medicamento = medicamentoCorrespondente,
            comprovanteImagem = command.comprovanteImagem?.value,
            dataSelecionada = command.dataSelecionada,
            horarioSelecionado = command.horarioSelecionado,
        )
    }

    private suspend fun encontrarMedicamentoCorrespondente(
        medicamentoCapturado: MedicamentoCapturadoDomain,
    ): MedicamentoDomain? = local.medications()
        .map { it.toDomain() }
        .mapNotNull { medicamentoSalvo ->
            if (
                MedicationTextMatcher.isMedicationMatch(
                    savedName = medicamentoSalvo.nome,
                    capturedName = medicamentoCapturado.nome,
                    savedActiveIngredient = medicamentoSalvo.compostoAtivo,
                    capturedActiveIngredient = medicamentoCapturado.compostoAtivo,
                )
            ) {
                MedicamentoMatch(
                    medicamento = medicamentoSalvo,
                    score = MedicationTextMatcher.medicationScore(
                        savedName = medicamentoSalvo.nome,
                        capturedName = medicamentoCapturado.nome,
                        savedActiveIngredient = medicamentoSalvo.compostoAtivo,
                        capturedActiveIngredient = medicamentoCapturado.compostoAtivo,
                    ),
                )
            } else {
                null
            }
        }
        .maxByOrNull { it.score }
        ?.medicamento

    private suspend fun processarConfirmacao(
        medicamento: MedicamentoDomain,
        comprovanteImagem: String?,
        dataSelecionada: String?,
        horarioSelecionado: String?,
    ) {
        val horarioConfirmacao = horarioSelecionado
            ?.take(5)
            ?.also { validarDoseSelecionada(dataSelecionada, it) }
            ?: encontrarHorarioMaisProximo(
                medicamento.frequenciaUso.horariosDoDia().map {
                    it.toString()
                },
            )
        val dataConfirmacao = dataSelecionada?.takeIf { it.isNotBlank() }
            ?: clock.localDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val confirmacaoExistente = local.confirmation(
            medicationId = medicamento.id,
            date = dataConfirmacao,
            time = horarioConfirmacao,
        )

        if (confirmacaoExistente?.sincronizado == true) {
            throw ConfirmationAlreadyExistsException()
        }

        val request = ConfirmacaoRequestDto(
            usuarioId = local.user().id,
            medicamentoId = medicamento.id,
            horario = horarioConfirmacao,
            data = dataConfirmacao,
            foiTomado = true,
            observacao = null,
        )

        remote.confirm(
            request,
            images.jpeg(comprovanteImagem, "confirmacao_${clock.instant().toEpochMilli()}.jpg"),
        )
        local.saveConfirmation(
            (
                confirmacaoExistente ?: ConfirmacaoEntity(
                    medicamentoId = medicamento.id,
                    horario = horarioConfirmacao,
                    data = dataConfirmacao,
                    foiTomado = true,
                )
                ).copy(foiTomado = true, sincronizado = true),
        )
    }

    private fun encontrarHorarioMaisProximo(horarios: List<String>): String {
        val horaAtual = clock.localTime()
        val horariosOrdenados = horarios
            .mapNotNull { horario -> runCatching { LocalTime.parse(horario.take(5)) }.getOrNull() }
            .sorted()

        return horariosOrdenados
            .lastOrNull { horarioDose -> !horarioDose.isAfter(horaAtual) }
            ?.format(DateTimeFormatter.ofPattern("HH:mm"))
            ?: throw DoseOutsideAllowedTimeException()
    }

    private fun validarDoseSelecionada(data: String?, horario: String) {
        val dataDose = data
            ?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it) }
            ?: clock.localDate()
        val horarioDose = LocalTime.parse(horario.take(5))
        val dataHoraDose = LocalDateTime.of(dataDose, horarioDose)

        if (dataHoraDose.isAfter(clock.localDateTime())) {
            throw DoseOutsideAllowedTimeException()
        }
    }

    private data class MedicamentoMatch(val medicamento: MedicamentoDomain, val score: Double)

    private fun <T> mapRemote(block: () -> T): T = try {
        block()
    } catch (error: Exception) {
        throw InvalidRemoteResponseException(error)
    }
}
