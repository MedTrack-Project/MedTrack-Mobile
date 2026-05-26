package com.example.piec_1.domain.model.mappers

import com.example.piec_1.data.local.entity.UsuarioEntity
import com.example.piec_1.domain.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Test

class UsuarioMappersTest {

    @Test
    fun `usuario domain maps to entity and back`() {
        val usuario = Usuario(
            id = 9,
            nome = "Joao Souza",
            email = "joao@example.com",
            nomeUsuario = "joao"
        )

        val entity = usuario.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(
            UsuarioEntity(
                id = 9,
                nome = "Joao Souza",
                email = "joao@example.com",
                nomeUsuario = "joao"
            ),
            entity
        )
        assertEquals(usuario, mappedBack)
    }
}
