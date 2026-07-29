package com.example.piec_1.data.remote

import com.example.piec_1.data.remote.dto.ConfirmacaoResponseDto
import com.example.piec_1.data.remote.dto.LoginRequestDto
import com.example.piec_1.data.remote.dto.LoginResponseDto
import com.example.piec_1.data.remote.dto.MedicamentoDto
import com.example.piec_1.data.remote.dto.ScanResponseDto
import com.example.piec_1.data.remote.dto.UsuarioDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface ApiService {

    @POST("auth/mobile/login")
    suspend fun login(@Body loginRequest: LoginRequestDto): Response<LoginResponseDto>

    @GET("usuario/mobile")
    suspend fun getUsuario(@Header("Authorization") token: String): Response<UsuarioDto>

    @GET("medicamento/mobile/lista")
    suspend fun getMedicamentos(@Header("Authorization") token: String): Response<List<MedicamentoDto>>

    @Multipart
    @POST("/api/confirmacao")
    suspend fun confirmarMedicamento(
        @Header("Authorization") token: String,
        @Part("dados") dados: RequestBody,
        @Part imagem: MultipartBody.Part? = null,
    ): Response<ConfirmacaoResponseDto>

    @Multipart
    @POST
    suspend fun scanMedicamento(
        @Url url: String,
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
    ): Response<ScanResponseDto>
}
