package com.example.piec_1.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.piec_1.data.local.AppDatabase
import com.example.piec_1.data.local.entity.ConfirmacaoEntity
import com.example.piec_1.data.remote.ApiService
import com.example.piec_1.data.remote.dto.ConfirmacaoRequestDto
import com.example.piec_1.data.remote.mapper.toDomain
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.domain.model.Usuario
import com.example.piec_1.domain.model.mappers.toDomain
import com.example.piec_1.domain.model.mappers.toEntity
import com.example.piec_1.domain.usecase.doseKey
import com.example.piec_1.domain.usecase.horariosDoDia
import com.example.piec_1.utils.exceptions.ConfirmacaoExistenteException
import com.example.piec_1.utils.exceptions.DoseForaDoHorarioException
import com.example.piec_1.utils.exceptions.MedicamentoNaoEncontradoException
import com.example.piec_1.utils.exceptions.TokenNaoEncontradoException
import com.example.piec_1.utils.notifications.NotificationScheduler
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink

@Singleton
class MedicamentoRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apiService: ApiService,
    database: AppDatabase,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler
) {
    private val usuarioDao = database.usuarioDao()
    private val medicamentoV2Dao = database.medicamentoV2Dao()
    private val confirmacaoDao = database.confirmacaoDao()
    private val gson = Gson()

    suspend fun sincronizarDadosDoUsuario(token: String): LoginData = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        val usuario = buscarUsuario(authHeader)
        val medicamentos = buscarMedicamentos(authHeader)

        usuarioDao.insert(usuario.toEntity())
        medicamentoV2Dao.insertAll(medicamentos.map { it.toEntity() })
        medicamentos.forEach { notificationScheduler.agendarNotificacao(it) }

        LoginData(
            token = token,
            usuario = usuario,
            medicamentos = medicamentos
        )
    }

    suspend fun buscarMedicamentoLocal(medicamentoId: Long): MedicamentoDomain? = withContext(Dispatchers.IO) {
        medicamentoV2Dao.getById(medicamentoId)?.toDomain()
    }

    suspend fun buscarChavesDeDosesConfirmadas(): Set<String> = withContext(Dispatchers.IO) {
        confirmacaoDao.getAll()
            .filter { it.sincronizado }
            .map { confirmacao ->
                doseKey(
                    medicamentoId = confirmacao.medicamentoId,
                    date = LocalDate.parse(confirmacao.data),
                    horario = confirmacao.horario.take(5)
                )
            }.toSet()
    }

    suspend fun confirmarMedicamento(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        medicamentoSelecionadoId: Long? = null,
        dataSelecionada: String? = null,
        horarioSelecionado: String? = null
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
            horarioSelecionado = horarioSelecionado
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
        medicamentoCapturado: MedicamentoCapturadoDomain
    ): MedicamentoDomain? {
        return medicamentoV2Dao.getAll()
            .map { it.toDomain() }
            .mapNotNull { medicamentoSalvo ->
                val nomeScore = similaridadeTexto(medicamentoSalvo.nome, medicamentoCapturado.nome)
                val compostoScore = similaridadeTexto(
                    medicamentoSalvo.compostoAtivo,
                    medicamentoCapturado.compostoAtivo
                )

                if (nomeScore >= MATCH_THRESHOLD && compostoScore >= MATCH_THRESHOLD) {
                    MedicamentoMatch(
                        medicamento = medicamentoSalvo,
                        score = (nomeScore + compostoScore) / 2.0
                    )
                } else {
                    null
                }
            }
            .maxByOrNull { it.score }
            ?.medicamento
    }

    private suspend fun processarConfirmacao(
        medicamento: MedicamentoDomain,
        token: String,
        comprovanteImagemUri: Uri?,
        dataSelecionada: String?,
        horarioSelecionado: String?
    ) {
        val horarioConfirmacao = horarioSelecionado
            ?.take(5)
            ?.also { validarDoseSelecionada(dataSelecionada, it) }
            ?: encontrarHorarioMaisProximo(
            medicamento.frequenciaUso.horariosDoDia().map { it.toString() }
        )
        val dataConfirmacao = dataSelecionada?.takeIf { it.isNotBlank() }
            ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val confirmacaoExistente = confirmacaoDao.getConfirmacao(
            medicamentoId = medicamento.id,
            data = dataConfirmacao,
            horario = horarioConfirmacao
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
            observacao = null
        )
        val imagemDisponivel = comprovanteImagemUri?.canOpenImage() == true

        Log.d(
            TAG_CONFIRMACAO,
            "Enviando confirmacao: usuarioId=${request.usuarioId}, " +
                "medicamentoId=${request.medicamentoId}, data=${request.data}, " +
                "horario=${request.horario}, foiTomado=${request.foiTomado}, " +
                "observacao=${request.observacao}, imagemDisponivel=$imagemDisponivel"
        )

        val response = apiService.confirmarMedicamento(
            token = "Bearer $token",
            dados = criarParteDados(request),
            imagem = criarParteImagem(comprovanteImagemUri)
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e(
                TAG_CONFIRMACAO,
                "Falha ao sincronizar confirmacao: httpCode=${response.code()}, body=$errorBody"
            )
            throw IOException(errorBody ?: "Erro na API")
        }

        Log.d(
            TAG_CONFIRMACAO,
            "Confirmacao aceita pelo backend: httpCode=${response.code()}, " +
                "responseId=${response.body()?.id}, " +
                "comprovanteImagemUrl=${response.body()?.comprovanteImagemUrl}"
        )

        if (confirmacaoExistente != null) {
            confirmacaoDao.update(
                confirmacaoExistente.copy(
                    foiTomado = true,
                    sincronizado = true
                )
            )
            Log.d(
                TAG_ROOM,
                "Confirmacao local atualizada como sincronizada: id=${confirmacaoExistente.id}, " +
                    "medicamentoId=${medicamento.id}, data=$dataConfirmacao, horario=$horarioConfirmacao"
            )
        } else {
            val confirmacaoId = confirmacaoDao.insert(
                ConfirmacaoEntity(
                    medicamentoId = medicamento.id,
                    horario = horarioConfirmacao,
                    data = dataConfirmacao,
                    foiTomado = true,
                    sincronizado = true
                )
            )
            Log.d(
                TAG_ROOM,
                "Confirmacao local inserida como sincronizada: id=$confirmacaoId, " +
                    "medicamentoId=${medicamento.id}, data=$dataConfirmacao, horario=$horarioConfirmacao"
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

    private fun similaridadeTexto(valorSalvo: String, valorCapturado: String): Double {
        val salvoNormalizado = normalizarTextoMedicamento(valorSalvo)
        val capturadoNormalizado = normalizarTextoMedicamento(valorCapturado)

        if (salvoNormalizado.isBlank() || capturadoNormalizado.isBlank()) return 0.0
        if (salvoNormalizado == capturadoNormalizado) return 1.0
        if (salvoNormalizado.contains(capturadoNormalizado) || capturadoNormalizado.contains(salvoNormalizado)) {
            return 0.92
        }

        val distancia = levenshteinDistance(salvoNormalizado, capturadoNormalizado)
        val maiorTamanho = maxOf(salvoNormalizado.length, capturadoNormalizado.length).coerceAtLeast(1)
        return 1.0 - (distancia.toDouble() / maiorTamanho.toDouble())
    }

    private fun normalizarTextoMedicamento(texto: String): String {
        val semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

        return semAcento
            .lowercase()
            .replace("0", "o")
            .replace("1", "i")
            .replace("5", "s")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { token -> token.isNotBlank() && token !in STOP_WORDS_MEDICAMENTO }
            .joinToString(" ")
            .trim()
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val previous = IntArray(b.length + 1) { it }
        val current = IntArray(b.length + 1)

        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(
                    current[j - 1] + 1,
                    previous[j] + 1,
                    previous[j - 1] + cost
                )
            }
            for (j in previous.indices) {
                previous[j] = current[j]
            }
        }

        return previous[b.length]
    }

    private fun criarParteDados(request: ConfirmacaoRequestDto): RequestBody {
        return gson.toJson(request).toRequestBody("application/json".toMediaType())
    }

    private fun criarParteImagem(uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        if (!uri.canOpenImage()) return null

        val requestBody = runCatching { uri.asJpegRequestBody() }.getOrNull() ?: return null
        return MultipartBody.Part.createFormData(
            name = "imagem",
            filename = "confirmacao_${System.currentTimeMillis()}.jpg",
            body = requestBody
        )
    }

    private fun Uri.asJpegRequestBody(): RequestBody {
        val contentResolver = context.contentResolver
        val imageMediaType = "image/jpeg".toMediaType()

        return object : RequestBody() {
            override fun contentType() = imageMediaType

            override fun writeTo(sink: BufferedSink) {
                val inputStream = runCatching {
                    contentResolver.openInputStream(this@asJpegRequestBody)
                }.getOrNull()
                    ?: path?.let { FileInputStream(File(it)) }
                    ?: throw IOException("Imagem da confirmacao indisponivel")

                inputStream.use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }

    private fun Uri.canOpenImage(): Boolean {
        return runCatching {
            context.contentResolver.openInputStream(this)?.use { true } ?: false
        }.getOrDefault(false) || path?.let { File(it).exists() } == true
    }

    private data class MedicamentoMatch(
        val medicamento: MedicamentoDomain,
        val score: Double
    )

    private companion object {
        const val TAG_CONFIRMACAO = "Confirmacao"
        const val TAG_ROOM = "Room"
        const val MATCH_THRESHOLD = 0.78
        val STOP_WORDS_MEDICAMENTO = setOf(
            "medicamento",
            "generico",
            "genérico",
            "comprimido",
            "capsula",
            "capsulas",
            "solucao",
            "oral",
            "uso",
            "adulto",
            "pediatrico"
        )
    }
}
