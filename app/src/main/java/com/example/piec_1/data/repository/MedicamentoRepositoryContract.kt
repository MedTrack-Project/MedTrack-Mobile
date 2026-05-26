package com.example.piec_1.data.repository

import android.net.Uri
import com.example.piec_1.domain.model.MedicamentoCapturadoDomain
import com.example.piec_1.domain.model.MedicamentoDomain

interface MedicamentoRepositoryContract {
    suspend fun sincronizarDadosDoUsuario(token: String): LoginData

    suspend fun buscarMedicamentoLocal(medicamentoId: Long): MedicamentoDomain?

    suspend fun buscarChavesDeDosesConfirmadas(): Set<String>

    suspend fun confirmarMedicamento(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        medicamentoSelecionadoId: Long? = null,
        dataSelecionada: String? = null,
        horarioSelecionado: String? = null
    )
}
