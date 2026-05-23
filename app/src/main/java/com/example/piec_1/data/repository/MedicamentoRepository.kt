package com.example.piec_1.data.repository

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicamentoRepository @Inject constructor(
    private val apiService: ApiService,
    database: AppDatabase,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler
) {
    private val usuarioDao = database.usuarioDao()
    private val medicamentoV2Dao = database.medicamentoV2Dao()
    private val confirmacaoDao = database.confirmacaoDao()

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
        confirmacaoDao.getAll().map { confirmacao ->
            doseKey(
                medicamentoId = confirmacao.medicamentoId,
                date = LocalDate.parse(confirmacao.data),
                horario = confirmacao.horario.take(5)
            )
        }.toSet()
    }

    suspend fun confirmarMedicamento(medicamentoCapturado: MedicamentoCapturadoDomain) = withContext(Dispatchers.IO) {
        val token = authRepository.getToken() ?: throw TokenNaoEncontradoException()
        val medicamentoCorrespondente = encontrarMedicamentoCorrespondente(medicamentoCapturado)
            ?: throw MedicamentoNaoEncontradoException()

        processarConfirmacao(medicamentoCorrespondente, token)
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
        return medicamentoV2Dao.getAll().map { it.toDomain() }.firstOrNull { medicamentoSalvo ->
            normalizarString(medicamentoSalvo.nome) == normalizarString(medicamentoCapturado.nome) &&
                normalizarString(medicamentoSalvo.compostoAtivo) == normalizarString(medicamentoCapturado.compostoAtivo) &&
                normalizarDosagem(medicamentoSalvo.dosagem) == normalizarDosagem(medicamentoCapturado.dosagem)
        }
    }

    private suspend fun processarConfirmacao(medicamento: MedicamentoDomain, token: String) {
        val horarioSelecionado = encontrarHorarioMaisProximo(
            medicamento.frequenciaUso.horariosDoDia().map { it.toString() }
        )
        val dataAtual = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val confirmacaoExistente = confirmacaoDao.getConfirmacao(
            medicamentoId = medicamento.id,
            data = dataAtual,
            horario = horarioSelecionado
        )

        if (confirmacaoExistente != null) {
            throw ConfirmacaoExistenteException()
        }

        val confirmacao = ConfirmacaoEntity(
            medicamentoId = medicamento.id,
            horario = horarioSelecionado,
            data = dataAtual,
            foiTomado = true
        )
        val confirmacaoId = confirmacaoDao.insert(confirmacao)

        val request = ConfirmacaoRequestDto(
            usuarioId = usuarioDao.getUsuario().id,
            medicamentoId = medicamento.id,
            horario = horarioSelecionado,
            data = dataAtual,
            foiTomado = true,
            observacao = null
        )

        val response = apiService.confirmarMedicamento("Bearer $token", request)

        if (!response.isSuccessful) {
            throw IOException(response.errorBody()?.string() ?: "Erro na API")
        }

        confirmacaoDao.update(confirmacao.copy(id = confirmacaoId, sincronizado = true))
    }

    private fun encontrarHorarioMaisProximo(horarios: List<String>): String {
        val horaAtual = LocalTime.now()
        val horariosOrdenados = horarios.sortedBy { LocalTime.parse(it) }

        return horariosOrdenados.lastOrNull { horario ->
            val horarioDose = LocalTime.parse(horario)
            !horarioDose.isAfter(horaAtual)
        } ?: throw DoseForaDoHorarioException()
    }

    private fun normalizarDosagem(dosagem: String): String {
        return dosagem.replace(" ", "").lowercase()
    }

    private fun normalizarString(texto: String): String {
        return texto.trim()
            .replace(Regex("[^a-zA-Z0-9]"), "")
            .lowercase()
    }
}
