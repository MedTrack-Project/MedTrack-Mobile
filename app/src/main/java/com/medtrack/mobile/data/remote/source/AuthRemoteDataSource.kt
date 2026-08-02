package com.medtrack.mobile.data.remote.source

import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.RemoteCallExecutor
import com.medtrack.mobile.data.remote.dto.LoginRequestDto
import com.medtrack.mobile.domain.error.InvalidCredentialsException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.RemoteRequestRejectedException
import javax.inject.Inject

interface AuthRemoteSource {
    suspend fun login(username: String, password: String): String
}

class AuthRemoteDataSource @Inject constructor(private val api: ApiService, private val calls: RemoteCallExecutor) :
    AuthRemoteSource {
    override suspend fun login(username: String, password: String): String = try {
        calls.execute { api.login(LoginRequestDto(username, password)) }.token
            .takeIf(String::isNotBlank)
            ?: throw InvalidCredentialsException()
    } catch (_: InvalidSessionException) {
        throw InvalidCredentialsException()
    } catch (error: RemoteRequestRejectedException) {
        if (error.statusCode == 400 || error.statusCode == 422) throw InvalidCredentialsException()
        throw error
    }
}
