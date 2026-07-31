package com.example.piec_1.core.config

import java.net.URI

data class ApiEndpoints(
    val apiBaseUrl: String,
    val scanUrl: String,
) {
    init {
        validate(name = "apiBaseUrl", value = apiBaseUrl, requireTrailingSlash = true)
        validate(name = "scanUrl", value = scanUrl, requireTrailingSlash = false)
    }

    private fun validate(name: String, value: String, requireTrailingSlash: Boolean) {
        val uri = runCatching { URI(value) }
            .getOrElse { throw IllegalArgumentException("$name must be a valid URL", it) }

        val scheme = uri.scheme?.lowercase()
        require(scheme == "http" || scheme == "https") { "$name must use HTTP or HTTPS" }
        require(!uri.host.isNullOrBlank()) { "$name must contain a valid host" }
        require(scheme != "http" || uri.host in LOCAL_HTTP_HOSTS) {
            "$name permits HTTP only for a local emulator host"
        }
        require(uri.userInfo == null) { "$name must not contain credentials" }
        require(uri.fragment == null) { "$name must not contain a fragment" }
        require(!requireTrailingSlash || value.endsWith('/')) { "$name must end with '/'" }
    }

    private companion object {
        val LOCAL_HTTP_HOSTS = setOf("10.0.2.2", "localhost")
    }
}
