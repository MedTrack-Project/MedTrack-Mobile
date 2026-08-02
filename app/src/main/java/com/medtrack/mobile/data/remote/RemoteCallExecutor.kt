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
        } catch (error: IOException) {
            if (error is MalformedJsonException || error.cause is MalformedJsonException) {
                throw InvalidRemoteResponseException(error)
            }
            throw NetworkUnavailableException(error)
        } catch (error: JsonParseException) {
            throw InvalidRemoteResponseException(error)
        }

        return when {
            response.code() == 401 || response.code() == 403 -> throw InvalidSessionException()
            response.code() >= 500 -> throw ServerUnavailableException()
            !response.isSuccessful -> throw RemoteRequestRejectedException(response.code())
            else -> response.body() ?: throw InvalidRemoteResponseException()
        }
    }
}
