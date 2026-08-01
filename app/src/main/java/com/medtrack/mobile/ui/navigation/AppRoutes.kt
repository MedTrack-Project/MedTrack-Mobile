package com.medtrack.mobile.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime

object AppRoutes {
    const val INICIAL = "TelaInicial"
    const val LOGIN = "TelaLogin"
    const val PRINCIPAL = "TelaPrincipal"
    const val ESQUECI_SENHA = "TelaEsqueciSenha"
    const val REDEFINIR_SENHA = "TelaRedefinirSenha"
    const val CONFIRMACAO = "TelaConfirmacao"
    const val CAMERA = "TelaCamera"
    const val DOSE_HORARIO = "TelaDoseHorario/{${Arguments.MEDICATION_ID}}/{${Arguments.DATE}}/{${Arguments.TIME}}"

    object Arguments {
        const val MEDICATION_ID = "medicamentoId"
        const val DATE = "data"
        const val TIME = "horario"
    }

    data class Dose(val medicationId: Long, val date: String, val time: String) {
        val route: String
            get() = "TelaDoseHorario/$medicationId/${date.encoded()}/${time.encoded()}"

        companion object {
            fun parse(medicationId: Long?, date: String?, time: String?): Dose? {
                if (medicationId == null || medicationId <= 0 || date == null || time == null) return null
                if (runCatching { LocalDate.parse(date) }.isFailure) return null
                if (runCatching { LocalTime.parse(time) }.isFailure) return null
                return Dose(medicationId, date, time)
            }
        }
    }

    fun doseHorario(medicamentoId: Long, data: String, horario: String): String =
        Dose(medicamentoId, data, horario).route

    fun doseHorarioDeepLink(medicamentoId: Long, data: String, horario: String): String =
        "app://telaDose/$medicamentoId/${data.encoded()}/${horario.encoded()}"

    private fun String.encoded(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.toString()).replace("+", "%20")
}
