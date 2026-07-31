package com.medtrack.mobile.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `string list converter serializes and deserializes values`() {
        val values = listOf("08:00", "14:30", "20:00")

        val json = converters.fromStringList(values)
        val mappedBack = converters.toStringList(json)

        assertEquals("""["08:00","14:30","20:00"]""", json)
        assertEquals(values, mappedBack)
    }

    @Test
    fun `string list converter handles empty list`() {
        val json = converters.fromStringList(emptyList())

        assertEquals("[]", json)
        assertEquals(emptyList<String>(), converters.toStringList(json))
    }
}
