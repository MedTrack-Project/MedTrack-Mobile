package com.example.piec_1.core.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiEndpointsTest {
    @Test
    fun `accepts valid HTTPS endpoints`() {
        val endpoints = ApiEndpoints(
            apiBaseUrl = "https://api.example.com/",
            scanUrl = "https://ai.example.com/detect",
        )

        assertEquals("https://api.example.com/", endpoints.apiBaseUrl)
        assertEquals("https://ai.example.com/detect", endpoints.scanUrl)
    }

    @Test
    fun `accepts local HTTP endpoints for debug configuration`() {
        val endpoints = ApiEndpoints(
            apiBaseUrl = "http://10.0.2.2:8081/",
            scanUrl = "http://10.0.2.2:8000/detect",
        )

        assertEquals("http://10.0.2.2:8081/", endpoints.apiBaseUrl)
    }

    @Test
    fun `rejects API base URL without trailing slash`() {
        assertThrows(IllegalArgumentException::class.java) {
            ApiEndpoints(
                apiBaseUrl = "https://api.example.com",
                scanUrl = "https://ai.example.com/detect",
            )
        }
    }

    @Test
    fun `rejects endpoint containing credentials`() {
        assertThrows(IllegalArgumentException::class.java) {
            ApiEndpoints(
                apiBaseUrl = "https://user:password@api.example.com/",
                scanUrl = "https://ai.example.com/detect",
            )
        }
    }

    @Test
    fun `rejects endpoint without HTTP scheme`() {
        assertThrows(IllegalArgumentException::class.java) {
            ApiEndpoints(
                apiBaseUrl = "ftp://api.example.com/",
                scanUrl = "https://ai.example.com/detect",
            )
        }
    }

    @Test
    fun `rejects cleartext endpoint outside local allowlist`() {
        assertThrows(IllegalArgumentException::class.java) {
            ApiEndpoints(
                apiBaseUrl = "http://api.example.com/",
                scanUrl = "https://ai.example.com/detect",
            )
        }
    }
}
