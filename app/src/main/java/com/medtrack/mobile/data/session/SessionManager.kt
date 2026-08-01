package com.medtrack.mobile.data.session

import android.content.Context
import androidx.core.content.edit
import com.medtrack.mobile.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) : SessionRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val KEY_TOKEN = "jwt_token"
    }
}
