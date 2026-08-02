package com.medtrack.mobile.data.session

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidTokenPreferences @Inject constructor(@ApplicationContext context: Context) : TokenPreferences {
    private val secure = context.getSharedPreferences(SECURE_PREFS, Context.MODE_PRIVATE)
    private val legacy = context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)

    override fun readEncrypted(): EncryptedToken? {
        val iv = secure.getString(KEY_IV, null) ?: return null
        val ciphertext = secure.getString(KEY_CIPHERTEXT, null) ?: return null
        return EncryptedToken(iv, ciphertext)
    }

    override fun writeEncrypted(value: EncryptedToken) {
        secure.edit(commit = true) {
            putString(KEY_IV, value.initializationVector)
            putString(KEY_CIPHERTEXT, value.ciphertext)
        }
    }

    override fun removeEncrypted() = secure.edit { clear() }
    override fun readLegacy(): String? = legacy.getString(LEGACY_TOKEN_KEY, null)
    override fun removeLegacy() = legacy.edit { remove(LEGACY_TOKEN_KEY) }

    private companion object {
        const val SECURE_PREFS = "medtrack_secure_session"
        const val KEY_IV = "iv"
        const val KEY_CIPHERTEXT = "ciphertext"
        const val LEGACY_PREFS = "MyAppPrefs"
        const val LEGACY_TOKEN_KEY = "jwt_token"
    }
}
