package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.remote.source.AuthRemoteSource
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.repository.AuthenticationRepository
import com.medtrack.mobile.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class AuthRepository @Inject constructor(
    private val remote: AuthRemoteSource,
    private val sessionRepository: SessionRepository,
    private val dispatchers: DispatcherProvider,
) : AuthenticationRepository {
    override suspend fun login(username: String, password: String): String = withContext(dispatchers.io) {
        val token = remote.login(username, password)
        sessionRepository.saveToken(token)
        token
    }
}
