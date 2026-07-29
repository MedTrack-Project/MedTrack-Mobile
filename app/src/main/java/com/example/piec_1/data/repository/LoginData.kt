package com.example.piec_1.data.repository

import com.example.piec_1.domain.model.MedicamentoDomain
import com.example.piec_1.domain.model.Usuario

data class LoginData(val token: String, val usuario: Usuario, val medicamentos: List<MedicamentoDomain>)
