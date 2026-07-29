package com.example.piec_1.data.remote.dto

data class LoginRequestDto(val username: String, val password: String)

data class LoginResponseDto(val token: String)
