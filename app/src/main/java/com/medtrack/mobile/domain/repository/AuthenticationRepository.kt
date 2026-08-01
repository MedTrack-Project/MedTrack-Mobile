package com.medtrack.mobile.domain.repository

interface AuthenticationRepository {
    suspend fun login(username: String, password: String): String
}
