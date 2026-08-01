package com.medtrack.mobile.domain.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

interface AppClock {
    fun instant(): Instant
    fun localDate(): LocalDate
    fun localTime(): LocalTime
    fun localDateTime(): LocalDateTime
    fun zoneId(): ZoneId
}
