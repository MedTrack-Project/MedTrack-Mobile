package com.medtrack.mobile.domain.model

data class ConfirmationCommand(
    val medicamentoCapturado: MedicamentoCapturadoDomain,
    val comprovanteImagem: ImageReference?,
    val medicamentoSelecionadoId: Long? = null,
    val dataSelecionada: String? = null,
    val horarioSelecionado: String? = null,
)

@JvmInline
value class ImageReference(val value: String)
