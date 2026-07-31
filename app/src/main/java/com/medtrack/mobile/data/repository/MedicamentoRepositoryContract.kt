package com.medtrack.mobile.data.repository

import android.net.Uri
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.domain.model.MedicamentoDomain

interface MedicamentoRepositoryContract {
    suspend fun sincronizarDadosDoUsuario(token: String): LoginData

    suspend fun buscarMedicamentoLocal(medicamentoId: Long): MedicamentoDomain?

    suspend fun buscarChavesDeDosesConfirmadas(): Set<String>

    suspend fun confirmarMedicamento(
        medicamentoCapturado: MedicamentoCapturadoDomain,
        comprovanteImagemUri: Uri?,
        medicamentoSelecionadoId: Long? = null,
        dataSelecionada: String? = null,
        horarioSelecionado: String? = null,
    )
}
