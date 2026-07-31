package com.medtrack.mobile.data.repository

import com.medtrack.mobile.domain.model.MedicamentoDomain
import com.medtrack.mobile.domain.model.Usuario

data class LoginData(val token: String, val usuario: Usuario, val medicamentos: List<MedicamentoDomain>)
