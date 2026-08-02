package com.medtrack.mobile.data.remote

import com.medtrack.mobile.domain.repository.SessionRepository
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(private val sessionRepository: SessionRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = sessionRepository.getToken()
        val authenticatedRequest = if (token.isNullOrBlank() || request.url.encodedPath.endsWith(LOGIN_PATH)) {
            request
        } else {
            request.newBuilder().header(AUTHORIZATION, "Bearer $token").build()
        }
        return chain.proceed(authenticatedRequest).also { response ->
            if (response.code == 401 && !request.url.encodedPath.endsWith(LOGIN_PATH)) {
                sessionRepository.clearToken()
            }
        }
    }

    private companion object {
        const val AUTHORIZATION = "Authorization"
        const val LOGIN_PATH = "/auth/mobile/login"
    }
}
