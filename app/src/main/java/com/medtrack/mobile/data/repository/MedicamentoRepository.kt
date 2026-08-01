package com.medtrack.mobile.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.medtrack.mobile.data.local.daos.ConfirmacaoDao
import com.medtrack.mobile.data.local.daos.MedicamentoV2Dao
import com.medtrack.mobile.data.local.daos.UsuarioDao
import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.data.mapper.local.toDomain
import com.medtrack.mobile.data.mapper.local.toEntity
import com.medtrack.mobile.data.mapper.remote.toDomain
import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.dto.ConfirmacaoRequestDto
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.error.ConfirmationAlreadyExistsException
import com.medtrack.mobile.domain.error.DoseOutsideAllowedTimeException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.MedicationNotFoundException
import com.medtrack.mobile.domain.error.RemoteDataException
import com.medtrack.mobile.domain.model.ConfirmationCommand
import com.medtrack.mobile.domain.model.LoginResult
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario
import com.medtrack.mobile.domain.repository.MedicationRepository
import com.medtrack.mobile.domain.repository.SessionRepository
import com.medtrack.mobile.domain.service.MedicationScheduler
import com.medtrack.mobile.domain.time.AppClock
import com.medtrack.mobile.domain.usecase.doseKey
import com.medtrack.mobile.domain.usecase.horariosDoDia
import com.medtrack.mobile.utils.MedicationTextMatcher
import com.medtrack.mobile.utils.MultipartImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

@Singleton
class MedicamentoRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val usuarioDao: UsuarioDao,
    private val medicamentoV2Dao: MedicamentoV2Dao,
    private val confirmacaoDao: ConfirmacaoDao,
    private val sessionRepository: SessionRepository,
    private val notificationScheduler: MedicationScheduler,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : MedicationRepository {
    private val gson = Gson()

    override suspend fun synchronizeUserData(token: String): LoginResult = withContext(dispatchers.io) {
        val authHeader = "Bearer $token"
        val usuario = buscarUsuario(authHeader)
        val medicamentos = buscarMedicamentos(authHeader)

        usuarioDao.insert(usuario.toEntity())
        medicamentoV2Dao.insertAll(medicamentos.map { it.toEntity() })
        medicamentos.forEach { notificationScheduler.schedule(it) }

        LoginResult(
            token = token,
            usuario = usuario,
            medicamentos = medicamentos,
        )
    }

    override suspend fun findMedication(medicamentoId: Long): MedicamentoDomain? = withContext(dispatchers.io) {
        medicamentoV2Dao.getById(medicamentoId)?.toDomain()
    }

    override suspend fun confirmedDoseKeys(): Set<String> = withContext(dispatchers.io) {
        confirmacaoDao.getAll()
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
        val token = sessionRepository.getToken() ?: throw InvalidSessionException()
        val medicamentoCorrespondente = command.medicamentoSelecionadoId
            ?.let { medicamentoV2Dao.getById(it)?.toDomain() }
            ?: encontrarMedicamentoCorrespondente(command.medicamentoCapturado)
            ?: throw MedicationNotFoundException()

        processarConfirmacao(
            medicamento = medicamentoCorrespondente,
            token = token,
            comprovanteImagemUri = command.comprovanteImagem?.value?.let(Uri::parse),
            dataSelecionada = command.dataSelecionada,
            horarioSelecionado = command.horarioSelecionado,
        )
    }

    private suspend fun buscarUsuario(authHeader: String): Usuario = executeRemote { apiService.getUsuario(authHeader) }
        ?.toDomain()
        ?: throw RemoteDataException()

    private suspend fun buscarMedicamentos(authHeader: String): List<MedicamentoDomain> = executeRemote {
        apiService.getMedicamentos(authHeader)
    }
        .orEmpty()
        .map { it.toDomain() }

    private suspend fun encontrarMedicamentoCorrespondente(
        medicamentoCapturado: MedicamentoCapturadoDomain,
    ): MedicamentoDomain? = medicamentoV2Dao.getAll()
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
        token: String,
        comprovanteImagemUri: Uri?,
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
        val confirmacaoExistente = confirmacaoDao.getConfirmacao(
            medicamentoId = medicamento.id,
            data = dataConfirmacao,
            horario = horarioConfirmacao,
        )

        if (confirmacaoExistente?.sincronizado == true) {
            throw ConfirmationAlreadyExistsException()
        }

        val request = ConfirmacaoRequestDto(
            usuarioId = usuarioDao.getUsuario().id,
            medicamentoId = medicamento.id,
            horario = horarioConfirmacao,
            data = dataConfirmacao,
            foiTomado = true,
            observacao = null,
        )

        executeRemote {
            apiService.confirmarMedicamento(
                token = "Bearer $token",
                dados = criarParteDados(request),
                imagem = criarParteImagem(comprovanteImagemUri),
            )
        }

        if (confirmacaoExistente != null) {
            confirmacaoDao.update(
                confirmacaoExistente.copy(
                    foiTomado = true,
                    sincronizado = true,
                ),
            )
        } else {
            confirmacaoDao.insert(
                ConfirmacaoEntity(
                    medicamentoId = medicamento.id,
                    horario = horarioConfirmacao,
                    data = dataConfirmacao,
                    foiTomado = true,
                    sincronizado = true,
                ),
            )
        }
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

    private fun criarParteDados(request: ConfirmacaoRequestDto): RequestBody =
        gson.toJson(request).toRequestBody("application/json".toMediaType())

    private fun criarParteImagem(uri: Uri?): MultipartBody.Part? = MultipartImageUtils.createJpegPart(
        context = context,
        uri = uri,
        partName = "imagem",
        filename = "confirmacao_${clock.instant().toEpochMilli()}.jpg",
    )

    private suspend fun <T> executeRemote(call: suspend () -> Response<T>): T? {
        val response = runCatching { call() }.getOrElse { throw RemoteDataException(it) }
        if (!response.isSuccessful) throw RemoteDataException()
        return response.body()
    }

    private data class MedicamentoMatch(val medicamento: MedicamentoDomain, val score: Double)
}
