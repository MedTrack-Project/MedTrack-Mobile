package com.medtrack.mobile.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `formatarHorario returns hour and minute for valid time`() {
        assertEquals("08:05", formatarHorario("08:05:30"))
    }

    @Test
    fun `toFormattedTime delegates to formatarHorario`() {
        assertEquals("21:15", "21:15:45".toFormattedTime())
    }
}
