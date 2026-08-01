package com.medtrack.mobile.ui.navigation

object AppIntentContract {
    const val ACTION_OPEN_CONFIRMATION = "com.medtrack.mobile.action.OPEN_CONFIRMATION"
    const val EXTRA_RESULT_REFERENCE = "com.medtrack.mobile.extra.RESULT_REFERENCE"

    fun confirmationReference(action: String?, reference: String?): String? =
        reference?.takeIf { action == ACTION_OPEN_CONFIRMATION && it.matches(Regex("[a-f0-9-]{36}")) }
}
