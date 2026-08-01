package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.dto.LoginRequestDto
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.error.InvalidCredentialsException
import com.medtrack.mobile.domain.error.RemoteDataException
import com.medtrack.mobile.domain.repository.AuthenticationRepository
import com.medtrack.mobile.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionRepository: SessionRepository,
    private val dispatchers: DispatcherProvider,
) : AuthenticationRepository {
    override suspend fun login(username: String, password: String): String = withContext(dispatchers.io) {
        val response = runCatching { apiService.login(LoginRequestDto(username, password)) }
            .getOrElse { throw RemoteDataException(it) }

        if (!response.isSuccessful) {
            throw InvalidCredentialsException()
        }

        val token = response.body()?.token
            ?: throw InvalidCredentialsException()

        sessionRepository.saveToken(token)
        token
    }
}
