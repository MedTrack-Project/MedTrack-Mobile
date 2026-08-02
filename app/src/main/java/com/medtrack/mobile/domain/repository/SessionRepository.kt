package com.medtrack.mobile.domain.repository

interface SessionRepository {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
