package com.medtrack.mobile.data.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerTest {
    @Test
    fun `legacy token is migrated without logout`() {
        val preferences = FakeTokenPreferences(legacy = "legacy-token")
        val manager = SessionManager(preferences, FakeTokenCipher())

        assertEquals("legacy-token", manager.getToken())
        assertNull(preferences.legacy)
        assertTrue(preferences.encrypted != null)
        assertEquals("legacy-token", manager.getToken())
    }

    @Test
    fun `corrupted encrypted token clears session`() {
        val preferences = FakeTokenPreferences(encrypted = EncryptedToken("iv", "corrupted"))
        val manager = SessionManager(preferences, FakeTokenCipher(failDecrypt = true))

        assertNull(manager.getToken())
        assertNull(preferences.encrypted)
    }
}

private class FakeTokenCipher(private val failDecrypt: Boolean = false) : TokenCipher {
    override fun encrypt(value: String) = EncryptedToken("iv", "encrypted:$value")
    override fun decrypt(value: EncryptedToken): String {
        if (failDecrypt) error("invalid")
        return value.ciphertext.removePrefix("encrypted:")
    }
}

private class FakeTokenPreferences(var encrypted: EncryptedToken? = null, var legacy: String? = null) :
    TokenPreferences {
    override fun readEncrypted() = encrypted
    override fun writeEncrypted(value: EncryptedToken) {
        encrypted = value
    }
    override fun removeEncrypted() {
        encrypted = null
    }
    override fun readLegacy() = legacy
    override fun removeLegacy() {
        legacy = null
    }
}
