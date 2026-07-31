package com.medtrack.mobile.utils

import java.text.Normalizer

object MedicationTextMatcher {
    private const val MATCH_THRESHOLD = 0.78

    private val stopWords = setOf(
        "medicamento",
        "generico",
        "genérico",
        "comprimido",
        "capsula",
        "capsulas",
        "solucao",
        "oral",
        "uso",
        "adulto",
        "pediatrico",
    )

    fun isMedicationMatch(
        savedName: String,
        capturedName: String,
        savedActiveIngredient: String,
        capturedActiveIngredient: String,
    ): Boolean = similarity(savedName, capturedName) >= MATCH_THRESHOLD &&
        similarity(savedActiveIngredient, capturedActiveIngredient) >= MATCH_THRESHOLD

    fun medicationScore(
        savedName: String,
        capturedName: String,
        savedActiveIngredient: String,
        capturedActiveIngredient: String,
    ): Double {
        val nameScore = similarity(savedName, capturedName)
        val activeIngredientScore = similarity(savedActiveIngredient, capturedActiveIngredient)
        return (nameScore + activeIngredientScore) / 2.0
    }

    private fun similarity(savedValue: String, capturedValue: String): Double {
        val savedNormalized = normalize(savedValue)
        val capturedNormalized = normalize(capturedValue)

        if (savedNormalized.isBlank() || capturedNormalized.isBlank()) return 0.0
        if (savedNormalized == capturedNormalized) return 1.0
        if (savedNormalized.contains(capturedNormalized) ||
            capturedNormalized.contains(savedNormalized)
        ) {
            return 0.92
        }

        val distance = levenshteinDistance(savedNormalized, capturedNormalized)
        val maxLength = maxOf(savedNormalized.length, capturedNormalized.length).coerceAtLeast(1)
        return 1.0 - (distance.toDouble() / maxLength.toDouble())
    }

    private fun normalize(text: String): String {
        val withoutAccent = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

        return withoutAccent
            .lowercase()
            .replace("0", "o")
            .replace("1", "i")
            .replace("5", "s")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { token -> token.isNotBlank() && token !in stopWords }
            .joinToString(" ")
            .trim()
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val previous = IntArray(b.length + 1) { it }
        val current = IntArray(b.length + 1)

        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(
                    current[j - 1] + 1,
                    previous[j] + 1,
                    previous[j - 1] + cost,
                )
            }
            for (j in previous.indices) {
                previous[j] = current[j]
            }
        }

        return previous[b.length]
    }
}
