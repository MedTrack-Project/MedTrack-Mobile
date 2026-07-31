package com.medtrack.mobile.data.repository

import android.content.Context
import android.net.Uri
import com.medtrack.mobile.data.local.AppDatabase
import com.medtrack.mobile.data.local.entity.ConfirmacaoEntity
import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.dto.ConfirmacaoRequestDto
import com.medtrack.mobile.data.remote.mapper.toDomain
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario
import com.medtrack.mobile.domain.model.mappers.toDomain
import com.medtrack.mobile.domain.model.mappers.toEntity
import com.medtrack.mobile.domain.usecase.doseKey
import com.medtrack.mobile.domain.usecase.horariosDoDia
import com.medtrack.mobile.utils.MedicationTextMatcher
import com.medtrack.mobile.utils.MultipartImageUtils
import com.medtrack.mobile.utils.exceptions.ConfirmacaoExistenteException
import com.medtrack.mobile.utils.exceptions.DoseForaDoHorarioException
import com.medtrack.mobile.utils.exceptions.MedicamentoNaoEncontradoException
import com.medtrack.mobile.utils.exceptions.TokenNaoEncontradoException
import com.medtrack.mobile.utils.notifications.NotificationScheduler
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class MedicamentoRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiService: ApiService,
    database: AppDatabase,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler,
) : MedicamentoRepositoryContract {
    private val usuarioDao = database.usuarioDao()
    private val medicamentoV2Dao = database.medicamentoV2Dao()
    private val confirmacaoDao = database.confirmacaoDao()
    private val gson = Gson()

    override suspend fun sincronizarDadosDoUsuario(token: String): LoginData = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        val usuario = buscarUsuario(authHeader)
        val medicamentos = buscarMedicamentos(authHeader)

        usuarioDao.insert(usuario.toEntity())
        medicamentoV2Dao.insertAll(medicamentos.map { it.toEntity() })
        medicamentos.forEach { notificationScheduler.agendarNotificacao(it) }

        LoginData(
            token = token,
            usuario = usuario,
            medicamentos = medicamentos,
        )
    }

    override suspend fun buscarMedicamentoLocal(medicamentoId: Long): MedicamentoDomain? = withContext(Dispatchers.IO) {
        medicamentoV2Dao.getById(medicamentoId)?.toDomain()
    }

    override suspend fun buscarChavesDeDosesConfirmadas(): Set<String> = withContext(Dispatchers.IO) {
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

    override suspend fun confirmarMedicamento(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        medicamentoSelecionadoId: Long?,
        dataSelecionada: String?,
        horarioSelecionado: String?,
    ) = withContext(Dispatchers.IO) {
        val token = authRepository.getToken() ?: throw TokenNaoEncontradoException()
        val medicamentoCorrespondente = medicamentoSelecionadoId
            ?.let { medicamentoV2Dao.getById(it)?.toDomain() }
            ?: encontrarMedicamentoCorrespondente(medicamentoCapturado)
            ?: throw MedicamentoNaoEncontradoException()

        processarConfirmacao(
            medicamento = medicamentoCorrespondente,
            token = token,
            comprovanteImagemUri = comprovanteImagemUri,
            dataSelecionada = dataSelecionada,
            horarioSelecionado = horarioSelecionado,
        )
    }

    private suspend fun buscarUsuario(authHeader: String): Usuario {
        val response = apiService.getUsuario(authHeader)

        if (!response.isSuccessful) {
            throw IOException(response.errorBody()?.string() ?: "Erro ao buscar usuario")
        }

        return response.body()?.toDomain() ?: throw IOException("Usuario nao encontrado")
    }

    private suspend fun buscarMedicamentos(authHeader: String): List<MedicamentoDomain> {
        val response = apiService.getMedicamentos(authHeader)

        if (!response.isSuccessful) {
            throw IOException(response.errorBody()?.string() ?: "Erro ao buscar medicamentos")
        }

        return response.body().orEmpty().map { it.toDomain() }
    }

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
            ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val confirmacaoExistente = confirmacaoDao.getConfirmacao(
            medicamentoId = medicamento.id,
            data = dataConfirmacao,
            horario = horarioConfirmacao,
        )

        if (confirmacaoExistente?.sincronizado == true) {
            throw ConfirmacaoExistenteException()
        }

        val request = ConfirmacaoRequestDto(
            usuarioId = usuarioDao.getUsuario().id,
            medicamentoId = medicamento.id,
            horario = horarioConfirmacao,
            data = dataConfirmacao,
            foiTomado = true,
            observacao = null,
        )

        val response = apiService.confirmarMedicamento(
            token = "Bearer $token",
            dados = criarParteDados(request),
            imagem = criarParteImagem(comprovanteImagemUri),
        )

        if (!response.isSuccessful) {
            throw IOException(response.errorBody()?.string() ?: "Erro na API")
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
        val horaAtual = LocalTime.now()
        val horariosOrdenados = horarios
            .mapNotNull { horario -> runCatching { LocalTime.parse(horario.take(5)) }.getOrNull() }
            .sorted()

        return horariosOrdenados
            .lastOrNull { horarioDose -> !horarioDose.isAfter(horaAtual) }
            ?.format(DateTimeFormatter.ofPattern("HH:mm"))
            ?: throw DoseForaDoHorarioException()
    }

    private fun validarDoseSelecionada(data: String?, horario: String) {
        val dataDose = data
            ?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it) }
            ?: LocalDate.now()
        val horarioDose = LocalTime.parse(horario.take(5))
        val dataHoraDose = LocalDateTime.of(dataDose, horarioDose)

        if (dataHoraDose.isAfter(LocalDateTime.now())) {
            throw DoseForaDoHorarioException()
        }
    }

    private fun criarParteDados(request: ConfirmacaoRequestDto): RequestBody =
        gson.toJson(request).toRequestBody("application/json".toMediaType())

    private fun criarParteImagem(uri: Uri?): MultipartBody.Part? = MultipartImageUtils.createJpegPart(
        context = context,
        uri = uri,
        partName = "imagem",
        filename = "confirmacao_${System.currentTimeMillis()}.jpg",
    )

    private data class MedicamentoMatch(val medicamento: MedicamentoDomain, val score: Double)
}
