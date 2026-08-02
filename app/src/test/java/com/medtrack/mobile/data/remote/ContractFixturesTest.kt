package com.medtrack.mobile.data.remote

import com.google.gson.Gson
import com.medtrack.mobile.data.remote.dto.ConfirmacaoResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ContractFixturesTest {
    @Test
    fun `confirmation fixture remains compatible with centralized gson`() {
        val response = Gson().fromJson(fixture("confirmation-success.json"), ConfirmacaoResponseDto::class.java)
        assertEquals(50, response.id)
        assertEquals("2026-08-02", response.data)
        assertEquals("08:00", response.horario)
    }

    private fun fixture(name: String): String = checkNotNull(
        javaClass.classLoader?.getResource("contracts/v1/$name"),
    ).readText()
}
