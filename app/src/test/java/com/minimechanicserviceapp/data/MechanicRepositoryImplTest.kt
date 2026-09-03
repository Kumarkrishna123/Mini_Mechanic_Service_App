package com.minimechanicserviceapp.data

import com.minimechanicserviceapp.core.result.AppResult
import com.minimechanicserviceapp.core.result.DataError
import com.minimechanicserviceapp.data.remote.MechanicApi
import com.minimechanicserviceapp.data.repository.MechanicRepositoryImpl
import com.minimechanicserviceapp.util.FakeMechanicDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MechanicRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: MechanicApi
    private lateinit var dao: FakeMechanicDao
    private lateinit var repository: MechanicRepositoryImpl

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MechanicApi::class.java)

        dao = FakeMechanicDao()
        repository = MechanicRepositoryImpl(api, dao)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun `a successful refresh writes through to the cache`() = runTest {
        enqueue(200, VALID_PAYLOAD)

        val result = repository.refreshMechanics()

        assertTrue(result is AppResult.Success)
        assertEquals(1, dao.current().size)
        assertEquals("Sharma Auto Works", dao.current().first().name)
    }

    @Test
    fun `observeMechanics exposes cached rows as domain models`() = runTest {
        enqueue(200, VALID_PAYLOAD)
        repository.refreshMechanics()

        val mechanics = repository.observeMechanics().first()

        assertEquals(1, mechanics.size)
        assertEquals("Sharma Auto Works", mechanics.first().name)
    }

    @Test
    fun `mockapi's injected fields do not break parsing`() = runTest {
        // createdAt and avatar are added by mockapi and were never sent by us
        enqueue(200, PAYLOAD_WITH_UNKNOWN_KEYS)

        val result = repository.refreshMechanics()

        assertTrue("unknown keys should be ignored", result is AppResult.Success)
        assertEquals(1, dao.current().size)
    }

    @Test
    fun `a server error maps to a typed Server failure`() = runTest {
        enqueue(500, "")

        val result = repository.refreshMechanics()

        assertEquals(AppResult.Failure(DataError.Server(500)), result)
    }

    @Test
    fun `a 404 carries its own status code`() = runTest {
        enqueue(404, "Not found")

        val result = repository.refreshMechanics()

        assertEquals(AppResult.Failure(DataError.Server(404)), result)
    }

    @Test
    fun `malformed json maps to a Serialization failure`() = runTest {
        enqueue(200, "{ this is not json")

        val result = repository.refreshMechanics()

        assertEquals(AppResult.Failure(DataError.Serialization), result)
    }

    @Test
    fun `an unreachable server maps to NoInternet`() = runTest {
        server.shutdown()

        val result = repository.refreshMechanics()

        assertEquals(AppResult.Failure(DataError.NoInternet), result)
    }

    @Test
    fun `a failed refresh leaves previously cached rows intact`() = runTest {
        enqueue(200, VALID_PAYLOAD)
        repository.refreshMechanics()
        enqueue(500, "")

        repository.refreshMechanics()

        assertEquals("cache must survive a failed refresh", 1, dao.current().size)
    }

    @Test
    fun `a malformed record is skipped while its siblings are kept`() = runTest {
        enqueue(200, PAYLOAD_WITH_ONE_BAD_RECORD)

        val result = repository.refreshMechanics()

        assertTrue(result is AppResult.Success)
        assertEquals(1, dao.current().size)
        assertEquals("Good Garage", dao.current().first().name)
    }

    private companion object {
        const val VALID_PAYLOAD = """
            [{"id":"1","name":"Sharma Auto Works","rating":4.6,"reviewCount":128,
              "address":"12 MG Road","locality":"Indiranagar",
              "latitude":12.9719,"longitude":77.6412,"phoneNumber":"+919876543210",
              "services":["Brake Repair"],
              "workingHours":[{"day":"MONDAY","opensAt":"09:00","closesAt":"19:00"}]}]
        """

        const val PAYLOAD_WITH_UNKNOWN_KEYS = """
            [{"createdAt":"2026-09-01T16:02:36.883Z","avatar":"https://example.com/a.jpg",
              "id":"1","name":"Sharma Auto Works","rating":4.6,"reviewCount":128,
              "address":"12 MG Road","locality":"Indiranagar",
              "latitude":12.9719,"longitude":77.6412,"phoneNumber":"+919876543210",
              "services":["Brake Repair"],"workingHours":[]}]
        """

        // the second record has no coordinates, so distance cannot be derived
        const val PAYLOAD_WITH_ONE_BAD_RECORD = """
            [{"id":"1","name":"Good Garage","rating":4.0,"reviewCount":5,
              "address":"A","locality":"B","latitude":12.97,"longitude":77.59,
              "phoneNumber":"+910000000000","services":[],"workingHours":[]},
             {"id":"2","name":"No Coordinates Garage","rating":3.0,"reviewCount":1,
              "address":"C","locality":"D","phoneNumber":"+910000000001",
              "services":[],"workingHours":[]}]
        """
    }
}
