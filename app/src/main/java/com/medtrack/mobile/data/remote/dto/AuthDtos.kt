package com.medtrack.mobile.data.remote.dto

data class LoginRequestDto(val username: String, val password: String)

data class LoginResponseDto(val token: String)
