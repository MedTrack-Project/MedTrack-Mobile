package com.medtrack.mobile.data.system

import com.medtrack.mobile.domain.time.AppClock
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class SystemClock @Inject constructor() : AppClock {
    private val clock: Clock get() = Clock.systemDefaultZone()

    override fun instant(): Instant = clock.instant()
    override fun localDate(): LocalDate = LocalDate.now(clock)
    override fun localTime(): LocalTime = LocalTime.now(clock)
    override fun localDateTime(): LocalDateTime = LocalDateTime.now(clock)
    override fun zoneId(): ZoneId = clock.zone
}
