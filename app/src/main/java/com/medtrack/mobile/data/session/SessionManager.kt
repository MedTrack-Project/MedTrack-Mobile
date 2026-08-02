package com.medtrack.mobile.data.session

import com.medtrack.mobile.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val preferences: TokenPreferences, private val cipher: TokenCipher) :
    SessionRepository {
    override fun saveToken(token: String) {
        require(token.isNotBlank()) { "Token must not be blank" }
        preferences.writeEncrypted(cipher.encrypt(token))
        check(preferences.readEncrypted()?.let(cipher::decrypt) == token) {
            "Protected session could not be verified"
        }
        preferences.removeLegacy()
    }

    override fun getToken(): String? {
        preferences.readEncrypted()?.let { encrypted ->
            return runCatching { cipher.decrypt(encrypted) }
                .onFailure { clearToken() }
                .getOrNull()
        }

        val legacyToken = preferences.readLegacy()?.takeIf(String::isNotBlank) ?: return null
        saveToken(legacyToken)
        return legacyToken
    }

    override fun clearToken() {
        preferences.removeEncrypted()
        preferences.removeLegacy()
    }
}

data class EncryptedToken(val initializationVector: String, val ciphertext: String)

interface TokenCipher {
    fun encrypt(value: String): EncryptedToken
    fun decrypt(value: EncryptedToken): String
}

interface TokenPreferences {
    fun readEncrypted(): EncryptedToken?
    fun writeEncrypted(value: EncryptedToken)
    fun removeEncrypted()
    fun readLegacy(): String?
    fun removeLegacy()
}
