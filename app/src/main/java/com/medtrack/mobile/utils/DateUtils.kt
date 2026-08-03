package com.medtrack.mobile.utils

import android.util.Log
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun formatarHorario(horario: String): String = try {
    LocalTime.parse(horario).format(DateTimeFormatter.ofPattern("HH:mm"))
} catch (_: DateTimeParseException) {
    Log.w("FormatarHorario", "Formato de horario invalido")
    if (horario.length >= 5) horario.substring(0, 5) else "--:--"
}

fun String.toFormattedTime(): String = formatarHorario(this)
