package com.medtrack.mobile.data.remote.source

import com.google.gson.Gson
import com.medtrack.mobile.data.remote.ApiService
import com.medtrack.mobile.data.remote.RemoteCallExecutor
import com.medtrack.mobile.data.remote.dto.ConfirmacaoRequestDto
import com.medtrack.mobile.data.remote.dto.MedicamentoDto
import com.medtrack.mobile.data.remote.dto.UsuarioDto
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface MedicationRemoteSource {
    suspend fun user(): UsuarioDto
    suspend fun medications(): List<MedicamentoDto>
    suspend fun confirm(request: ConfirmacaoRequestDto, image: MultipartBody.Part?)
}

class MedicationRemoteDataSource @Inject constructor(
    private val api: ApiService,
    private val calls: RemoteCallExecutor,
    private val gson: Gson,
) : MedicationRemoteSource {
    override suspend fun user(): UsuarioDto = calls.execute { api.getUsuario() }

    override suspend fun medications(): List<MedicamentoDto> = calls.execute { api.getMedicamentos() }

    override suspend fun confirm(request: ConfirmacaoRequestDto, image: MultipartBody.Part?) {
        calls.execute {
            api.confirmarMedicamento(
                dados = gson.toJson(request).toRequestBody(JSON_MEDIA_TYPE),
                imagem = image,
            )
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
