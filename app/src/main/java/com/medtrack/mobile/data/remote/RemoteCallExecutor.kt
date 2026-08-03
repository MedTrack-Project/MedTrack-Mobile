package com.medtrack.mobile.data.remote

import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.medtrack.mobile.domain.error.InvalidRemoteResponseException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.NetworkUnavailableException
import com.medtrack.mobile.domain.error.RemoteRequestRejectedException
import com.medtrack.mobile.domain.error.ServerUnavailableException
import java.io.IOException
import javax.inject.Inject
import retrofit2.Response

class RemoteCallExecutor @Inject constructor() {
    suspend fun <T : Any> execute(call: suspend () -> Response<T>): T {
        val response = try {
            call()
        } catch (error: MalformedJsonException) {
            invalidResponse(error)
        } catch (error: IOException) {
            networkUnavailable(error)
        } catch (error: JsonParseException) {
            invalidResponse(error)
        }

        return when {
            response.code() == 401 || response.code() == 403 -> invalidSession()
            response.code() >= 500 -> serverUnavailable()
            !response.isSuccessful -> rejected(response.code())
            else -> response.body() ?: invalidResponse()
        }
    }

    private fun invalidSession(): Nothing = throw InvalidSessionException()
    private fun serverUnavailable(): Nothing = throw ServerUnavailableException()
    private fun rejected(statusCode: Int): Nothing = throw RemoteRequestRejectedException(statusCode)
    private fun invalidResponse(): Nothing = throw InvalidRemoteResponseException()
    private fun invalidResponse(cause: Throwable): Nothing = throw InvalidRemoteResponseException(cause)
    private fun networkUnavailable(cause: IOException): Nothing = throw NetworkUnavailableException(cause)
}
