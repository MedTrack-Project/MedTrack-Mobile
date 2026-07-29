package com.example.piec_1.ui.navigation

import android.net.Uri

object AppRoutes {
    const val INICIAL = "TelaInicial"
    const val LOGIN = "TelaLogin"
    const val PRINCIPAL = "TelaPrincipal"
    const val ESQUECI_SENHA = "TelaEsqueciSenha"
    const val REDEFINIR_SENHA = "TelaRedefinirSenha"
    const val CONFIRMACAO = "TelaConfirmacao"
    const val CAMERA = "TelaCamera"
    const val CAMERA_FROM_NOTIFICATION = "TelaCamera/{medicamentoId}/{horario}"
    const val DOSE_HORARIO = "TelaDoseHorario/{medicamentoId}/{data}/{horario}"

    fun cameraDeepLink(medicamentoId: Long, horario: String): String = "app://telaCamera/$medicamentoId/$horario"

    fun doseHorario(medicamentoId: Long, data: String, horario: String): String =
        "TelaDoseHorario/$medicamentoId/${Uri.encode(data)}/${Uri.encode(horario)}"

    fun doseHorarioDeepLink(medicamentoId: Long, data: String, horario: String): String =
        "app://telaDose/$medicamentoId/${Uri.encode(data)}/${Uri.encode(horario)}"
}
