package com.medtrack.mobile.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppNavigationContractTest {
    @Test
    fun `dose route accepts valid arguments`() {
        assertEquals(
            AppRoutes.Dose(7, "2026-08-01", "08:30"),
            AppRoutes.Dose.parse(7, "2026-08-01", "08:30"),
        )
    }

    @Test
    fun `dose route rejects invalid arguments`() {
        assertNull(AppRoutes.Dose.parse(0, "2026-08-01", "08:30"))
        assertNull(AppRoutes.Dose.parse(7, "01-08-2026", "08:30"))
        assertNull(AppRoutes.Dose.parse(7, "2026-08-01", "25:00"))
    }

    @Test
    fun `notification accepts only centralized action and uuid reference`() {
        val reference = "123e4567-e89b-12d3-a456-426614174000"
        assertEquals(
            reference,
            AppIntentContract.confirmationReference(AppIntentContract.ACTION_OPEN_CONFIRMATION, reference),
        )
        assertNull(AppIntentContract.confirmationReference("invalid", reference))
        assertNull(AppIntentContract.confirmationReference(AppIntentContract.ACTION_OPEN_CONFIRMATION, "../payload"))
    }
}
