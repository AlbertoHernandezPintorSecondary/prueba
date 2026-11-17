package com.example.pruebatecnica.data.remote

import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.fixtures.failureError
import com.example.pruebatecnica.domain.fixtures.successData
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Verifies request building, JSON parsing into response models (DTOs) and HTTP -> DomainError
 * translation against a local fake server. DTO -> domain mapping is covered in CharacterRepositoryImplTest.
 */
class RetrofitCharacterRemoteDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitCharacterRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = RetrofitCharacterRemoteDataSource(RetrofitFactory.createApi(server.url("/api/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search parses results and sends name and page`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(searchResponse(next = "https://rickandmortyapi.com/api/character/?page=2&name=rick")))

        val result = dataSource.searchCharacters("rick", page = 1).successData()

        val request = server.takeRequest()
        assertEquals("/api/character/?name=rick&page=1", request.path)
        assertEquals("https://rickandmortyapi.com/api/character/?page=2&name=rick", result.info.next)
        assertEquals(1, result.results.size)
        with(result.results.first()) {
            assertEquals(1, id)
            assertEquals("Rick Sanchez", name)
            assertEquals("Alive", status)
            assertEquals("Earth (C-137)", origin.name)
            assertEquals("Citadel of Ricks", location.name)
            assertEquals(2, episode.size)
        }
    }

    @Test
    fun `last page has no next url`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(searchResponse(next = null)))

        val result = dataSource.searchCharacters("rick", page = 3).successData()

        assertNull(result.info.next)
    }

    @Test
    fun `search with 404 returns NotFound`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"There is nothing here"}"""))

        val error = dataSource.searchCharacters("zzzz", page = 1).failureError()

        assertEquals(DomainError.NotFound, error)
    }

    @Test
    fun `detail with 404 returns NotFound`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"Character not found"}"""))

        val error = dataSource.getCharacter(9999).failureError()

        assertEquals(DomainError.NotFound, error)
    }

    @Test
    fun `server errors return Server with code`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val error = dataSource.searchCharacters("rick", page = 1).failureError()

        assertEquals(DomainError.Server(500), error)
    }

    @Test
    fun `malformed response returns Unknown`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"unexpected": true}"""))

        val error = dataSource.searchCharacters("rick", page = 1).failureError()

        assertTrue(error is DomainError.Unknown)
    }

    @Test
    fun `connection failures return Network with the original cause`() = runTest {
        // Nothing listens on port 1, so the connection is refused (IOException).
        val unreachable = RetrofitCharacterRemoteDataSource(RetrofitFactory.createApi("http://localhost:1/api/"))

        val error = unreachable.searchCharacters("rick", page = 1).failureError()

        assertTrue(error is DomainError.Network)
        assertTrue("cause kept for diagnostics", error.cause is IOException)
    }

    private fun searchResponse(next: String?): String {
        val nextJson = if (next == null) "null" else "\"$next\""
        return """
            {
              "info": { "count": 1, "pages": 1, "next": $nextJson, "prev": null },
              "results": [
                {
                  "id": 1,
                  "name": "Rick Sanchez",
                  "status": "Alive",
                  "species": "Human",
                  "type": "",
                  "gender": "Male",
                  "origin": { "name": "Earth (C-137)", "url": "https://rickandmortyapi.com/api/location/1" },
                  "location": { "name": "Citadel of Ricks", "url": "https://rickandmortyapi.com/api/location/3" },
                  "image": "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
                  "episode": [
                    "https://rickandmortyapi.com/api/episode/1",
                    "https://rickandmortyapi.com/api/episode/2"
                  ],
                  "url": "https://rickandmortyapi.com/api/character/1",
                  "created": "2017-11-04T18:48:46.250Z"
                }
              ]
            }
        """.trimIndent()
    }
}
