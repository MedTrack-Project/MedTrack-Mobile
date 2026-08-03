package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.remote.source.AuthRemoteSource
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.error.InvalidCredentialsException
import com.medtrack.mobile.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {
    @Test
    fun `successful login persists token only after remote authentication`() = runTest {
        val session = RecordingSessionRepository()
        val repository = AuthRepository(
            remote = StubAuthRemoteSource(token = "access-token"),
            sessionRepository = session,
            dispatchers = RepositoryTestDispatchers,
        )

        val result = repository.login("yann", "secret")

        assertEquals("access-token", result)
        assertEquals("access-token", session.storedToken)
    }

    @Test
    fun `failed login never mutates the existing session`() {
        val session = RecordingSessionRepository()
        val repository = AuthRepository(
            remote = StubAuthRemoteSource(failure = InvalidCredentialsException()),
            sessionRepository = session,
            dispatchers = RepositoryTestDispatchers,
        )

        assertThrows(InvalidCredentialsException::class.java) {
            runTest { repository.login("invalid", "invalid") }
        }
        assertNull(session.storedToken)
    }
}

private class StubAuthRemoteSource(private val token: String = "", private val failure: Exception? = null) :
    AuthRemoteSource {
    override suspend fun login(username: String, password: String): String {
        failure?.let { throw it }
        return token
    }
}

private class RecordingSessionRepository : SessionRepository {
    var storedToken: String? = null

    override fun saveToken(token: String) {
        storedToken = token
    }

    override fun getToken(): String? = storedToken

    override fun clearToken() {
        storedToken = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal object RepositoryTestDispatchers : DispatcherProvider {
    override val io: CoroutineDispatcher = UnconfinedTestDispatcher()
    override val default: CoroutineDispatcher = io
}
