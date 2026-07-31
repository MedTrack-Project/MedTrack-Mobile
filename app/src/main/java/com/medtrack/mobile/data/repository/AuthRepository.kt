package com.medtrack.mobile.data.repository

import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.dto.LoginRequestDto
import com.medtrack.mobile.data.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
) {
    suspend fun login(username: String, password: String): String = withContext(Dispatchers.IO) {
        val response = apiService.login(LoginRequestDto(username, password))

        if (!response.isSuccessful) {
            throw LoginException("Usuario ou senha invalidos")
        }

        val token = response.body()?.token
            ?: throw LoginException("Token invalido")

        sessionManager.saveToken(token)
        token
    }

    fun getToken(): String? = sessionManager.getToken()
}

class LoginException(message: String) : Exception(message)
