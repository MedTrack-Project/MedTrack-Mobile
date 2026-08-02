package com.medtrack.mobile.data.remote

import com.medtrack.mobile.data.remote.dto.ConfirmacaoResponseDto
import com.medtrack.mobile.data.remote.dto.LoginRequestDto
import com.medtrack.mobile.data.remote.dto.LoginResponseDto
import com.medtrack.mobile.data.remote.dto.MedicamentoDto
import com.medtrack.mobile.data.remote.dto.ScanResponseDto
import com.medtrack.mobile.data.remote.dto.UsuarioDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface ApiService {

    @POST("auth/mobile/login")
    suspend fun login(@Body loginRequest: LoginRequestDto): Response<LoginResponseDto>

    @GET("usuario/mobile")
    suspend fun getUsuario(): Response<UsuarioDto>

    @GET("medicamento/mobile/lista")
    suspend fun getMedicamentos(): Response<List<MedicamentoDto>>

    @Multipart
    @POST("/api/confirmacao")
    suspend fun confirmarMedicamento(
        @Part("dados") dados: RequestBody,
        @Part imagem: MultipartBody.Part? = null,
    ): Response<ConfirmacaoResponseDto>

    @Multipart
    @POST
    suspend fun scanMedicamento(@Url url: String, @Part image: MultipartBody.Part): Response<ScanResponseDto>
}
