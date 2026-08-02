package com.medtrack.mobile.data.remote

import com.google.gson.Gson
import com.medtrack.mobile.core.config.ApiEndpoints
import com.medtrack.mobile.data.remote.source.AuthRemoteDataSource
import com.medtrack.mobile.data.remote.source.MedicationRemoteDataSource
import com.medtrack.mobile.data.remote.source.ScanRemoteDataSource
import com.medtrack.mobile.domain.error.InvalidRemoteResponseException
import com.medtrack.mobile.domain.error.InvalidSessionException
import com.medtrack.mobile.domain.error.NetworkUnavailableException
import com.medtrack.mobile.domain.error.RemoteRequestRejectedException
import com.medtrack.mobile.domain.error.ServerUnavailableException
import com.medtrack.mobile.domain.repository.SessionRepository
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiContractTest {
    private lateinit var server: MockWebServer
    private lateinit var session: ContractSession

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        session = ContractSession("stored-token")
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `login fixture is compatible and login omits authorization`() = runTest {
        server.enqueue(jsonResponse(fixture("login-success.json")))
        val remote = AuthRemoteDataSource(api(), RemoteCallExecutor())

        assertEquals("jwt-contract-token", remote.login("yann", "secret"))
        val request = server.takeRequest()
        assertEquals("/auth/mobile/login", request.path)
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `authenticated endpoint adds bearer token and parses fixture`() = runTest {
        server.enqueue(jsonResponse(fixture("user-success.json")))
        val user = MedicationRemoteDataSource(api(), RemoteCallExecutor(), Gson()).user()

        assertEquals("Yann", user.nome)
        assertEquals("Bearer stored-token", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `medication list fixture is compatible`() = runTest {
        server.enqueue(jsonResponse(fixture("medications-success.json")))
        val medications = MedicationRemoteDataSource(api(), RemoteCallExecutor(), Gson()).medications()
        assertEquals("Losartana", medications.single().nome)
    }

    @Test
    fun `unauthorized response expires session`() {
        server.enqueue(MockResponse().setResponseCode(401))
        val remote = MedicationRemoteDataSource(api(), RemoteCallExecutor(), Gson())

        assertThrows(InvalidSessionException::class.java) { runTest { remote.user() } }
        assertNull(session.getToken())
    }

    @Test
    fun `server error has stable domain type`() {
        server.enqueue(MockResponse().setResponseCode(503))
        val remote = MedicationRemoteDataSource(api(), RemoteCallExecutor(), Gson())
        assertThrows(ServerUnavailableException::class.java) { runTest { remote.user() } }
    }

    @Test
    fun `client error preserves rejected status`() {
        server.enqueue(MockResponse().setResponseCode(422))
        val remote = MedicationRemoteDataSource(api(), RemoteCallExecutor(), Gson())
        val error = assertThrows(RemoteRequestRejectedException::class.java) { runTest { remote.user() } }
        assertEquals(422, error.statusCode)
    }

    @Test
    fun `invalid payload has stable domain type`() {
        server.enqueue(jsonResponse("{invalid"))
        val remote = MedicationRemoteDataSource(api(), RemoteCallExecutor(), Gson())
        assertThrows(InvalidRemoteResponseException::class.java) { runTest { remote.user() } }
    }

    @Test
    fun `timeout has stable domain type`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val remote = MedicationRemoteDataSource(api(readTimeoutMillis = 50), RemoteCallExecutor(), Gson())
        assertThrows(NetworkUnavailableException::class.java) { runTest { remote.user() } }
    }

    @Test
    fun `scan uses the single frozen multipart field`() = runTest {
        server.enqueue(jsonResponse(fixture("scan-success.json")))
        val file = File.createTempFile("scan-contract", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        try {
            val remote = ScanRemoteDataSource(
                api(),
                RemoteCallExecutor(),
                ApiEndpoints(server.url("/").toString(), server.url("/detect").toString()),
            )
            assertEquals("Losartana", remote.scan(file)?.nome)
            val body = server.takeRequest().body.readUtf8()
            assertTrue(body.contains("name=\"file\""))
            assertFalse(body.contains("name=\"image\""))
            assertFalse(body.contains("name=\"photo\""))
        } finally {
            file.delete()
        }
    }

    private fun api(readTimeoutMillis: Long = 1_000): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(session))
            .readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }

    private fun fixture(name: String): String = checkNotNull(
        javaClass.classLoader?.getResource("contracts/v1/$name"),
    ).readText()

    private fun jsonResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}

private class ContractSession(private var token: String?) : SessionRepository {
    override fun saveToken(token: String) {
        this.token = token
    }
    override fun getToken(): String? = token
    override fun clearToken() {
        token = null
    }
}
