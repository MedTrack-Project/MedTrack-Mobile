package com.medtrack.mobile.domain.model

data class LoginResult(val token: String, val usuario: Usuario, val medicamentos: List<MedicamentoDomain>)
