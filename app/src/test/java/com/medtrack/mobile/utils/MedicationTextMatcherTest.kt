package com.medtrack.mobile.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationTextMatcherTest {

    @Test
    fun `isMedicationMatch accepts equivalent medication names after normalization`() {
        val result = MedicationTextMatcher.isMedicationMatch(
            savedName = "Losartana Potassica",
            capturedName = "LOSARTANA potassica comprimido",
            savedActiveIngredient = "Losartana Potassica",
            capturedActiveIngredient = "losartana potassica",
        )

        assertTrue(result)
    }

    @Test
    fun `isMedicationMatch accepts OCR-like substitutions`() {
        val result = MedicationTextMatcher.isMedicationMatch(
            savedName = "Sinvastatina",
            capturedName = "5invastatina",
            savedActiveIngredient = "Sinvastatina",
            capturedActiveIngredient = "sinvastatina",
        )

        assertTrue(result)
    }

    @Test
    fun `isMedicationMatch rejects different medication or active ingredient`() {
        val result = MedicationTextMatcher.isMedicationMatch(
            savedName = "Losartana",
            capturedName = "Metformina",
            savedActiveIngredient = "Losartana Potassica",
            capturedActiveIngredient = "Cloridrato de Metformina",
        )

        assertFalse(result)
    }

    @Test
    fun `medicationScore returns full score for exact normalized match`() {
        val score = MedicationTextMatcher.medicationScore(
            savedName = "Dipirona Sodica",
            capturedName = "dipirona sodica",
            savedActiveIngredient = "Dipirona",
            capturedActiveIngredient = "dipirona",
        )

        assertEquals(1.0, score, 0.0)
    }

    @Test
    fun `medicationScore returns zero when captured values are blank after normalization`() {
        val score = MedicationTextMatcher.medicationScore(
            savedName = "Atenolol",
            capturedName = "",
            savedActiveIngredient = "Atenolol",
            capturedActiveIngredient = "   ",
        )

        assertEquals(0.0, score, 0.0)
    }
}
