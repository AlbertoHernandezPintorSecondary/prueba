package com.example.pruebatecnica.data.repository

import com.example.pruebatecnica.data.FakeTimeProvider
import com.example.pruebatecnica.data.aCharacterDto
import com.example.pruebatecnica.data.aSearchResponseDto
import com.example.pruebatecnica.data.cache.InMemoryCache
import com.example.pruebatecnica.data.local.InMemoryCharacterLocalDataSource
import com.example.pruebatecnica.data.remote.CharacterRemoteDataSource
import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.fixtures.failureError
import com.example.pruebatecnica.domain.fixtures.successData
import com.example.pruebatecnica.domain.model.CharacterStatus
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import com.example.pruebatecnica.domain.result.DomainResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CharacterRepositoryImplTest {

    private val ttl = 5.minutes
    private lateinit var clock: FakeTimeProvider
    private lateinit var remote: CharacterRemoteDataSource
    private lateinit var repository: CharacterRepositoryImpl

    /** Shared with runTest so the repository's withContext(ioDispatcher) runs on the test scheduler. */
    private val testDispatcher = UnconfinedTestDispatcher()

    private val ricksPage1 = DomainResult.Success(
        aSearchResponseDto(
            aCharacterDto(1, "Rick Sanchez"),
            aCharacterDto(8, "Adjudicator Rick", status = "Dead"),
            hasNext = true,
        ),
    )
    private val ricksPage2 = DomainResult.Success(aSearchResponseDto(aCharacterDto(15, "Alien Rick"), hasNext = false))

    private fun List<RickAndMortyCharacter>.ids() = map { it.id }

    @Before
    fun setUp() {
        clock = FakeTimeProvider()
        remote = mockk()
        // Real in-memory local data source with a fake clock: these tests verify the cache behaviour end to end.
        val local = InMemoryCharacterLocalDataSource(
            searchCache = InMemoryCache(ttl = ttl, timeProvider = clock),
            characterCache = InMemoryCache(ttl = ttl, timeProvider = clock),
        )
        repository = CharacterRepositoryImpl(
            remoteDataSource = remote,
            localDataSource = local,
            ioDispatcher = testDispatcher,
        )
    }

    // region mapping (response model -> domain model)

    @Test
    fun `maps the search response to a domain page`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 1) } returns ricksPage1

        val page = repository.searchCharacters("rick", 1).successData()

        assertEquals(listOf(1, 8), page.characters.ids())
        assertEquals("next url present -> next page number", 2, page.nextPage)
        assertEquals(CharacterStatus.ALIVE, page.characters[0].status)
        assertEquals(CharacterStatus.DEAD, page.characters[1].status)
    }

    @Test
    fun `last page maps to no next page`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 2) } returns ricksPage2

        val page = repository.searchCharacters("rick", 2).successData()

        assertNull(page.nextPage)
    }

    @Test
    fun `maps every character field`() = runTest(testDispatcher) {
        coEvery { remote.getCharacter(15) } returns DomainResult.Success(
            aCharacterDto(15, "Alien Rick", status = "unknown", type = "Parasite", episodes = 3),
        )

        val character = repository.getCharacter(15).successData()

        assertEquals(
            RickAndMortyCharacter(
                id = 15,
                name = "Alien Rick",
                status = CharacterStatus.UNKNOWN,
                species = "Human",
                type = "Parasite",
                gender = "Male",
                originName = "Earth (C-137)",
                locationName = "Citadel of Ricks",
                imageUrl = "https://rickandmortyapi.com/api/character/avatar/15.jpeg",
                episodeCount = 3,
            ),
            character,
        )
    }

    // endregion

    // region cache

    @Test
    fun `repeated search within ttl is served from cache without network`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 1) } returns ricksPage1

        val first = repository.searchCharacters("rick", 1)
        clock.advanceBy(ttl - 1.seconds)
        val second = repository.searchCharacters("rick", 1)

        assertEquals(first, second)
        coVerify(exactly = 1) { remote.searchCharacters("rick", 1) }
    }

    @Test
    fun `cache key is normalized so case and spaces do not matter`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 1) } returns ricksPage1

        repository.searchCharacters("Rick", 1)
        repository.searchCharacters("  RICK ", 1)

        coVerify(exactly = 1) { remote.searchCharacters("rick", 1) }
    }

    @Test
    fun `each page of the same query is cached independently`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 1) } returns ricksPage1
        coEvery { remote.searchCharacters("rick", 2) } returns ricksPage2

        repository.searchCharacters("rick", 1)
        repository.searchCharacters("rick", 2)
        val page1Again = repository.searchCharacters("rick", 1).successData()
        val page2Again = repository.searchCharacters("rick", 2).successData()

        assertEquals(listOf(1, 8), page1Again.characters.ids())
        assertEquals(listOf(15), page2Again.characters.ids())
        coVerify(exactly = 1) { remote.searchCharacters("rick", 1) }
        coVerify(exactly = 1) { remote.searchCharacters("rick", 2) }
    }

    @Test
    fun `expired entry is fetched again from network and cache is updated`() = runTest(testDispatcher) {
        val updated = DomainResult.Success(
            aSearchResponseDto(
                aCharacterDto(1, "Rick Sanchez"),
                aCharacterDto(8, "Adjudicator Rick"),
                aCharacterDto(99, "New Rick"),
                hasNext = true,
            ),
        )
        coEvery { remote.searchCharacters("rick", 1) } returnsMany listOf(ricksPage1, updated)

        repository.searchCharacters("rick", 1)
        clock.advanceBy(ttl)
        val afterExpiry = repository.searchCharacters("rick", 1).successData()
        clock.advanceBy(1.minutes)
        val fromRefreshedCache = repository.searchCharacters("rick", 1).successData()

        assertEquals(listOf(1, 8, 99), afterExpiry.characters.ids())
        assertEquals(afterExpiry, fromRefreshedCache)
        coVerify(exactly = 2) { remote.searchCharacters("rick", 1) }
    }

    @Test
    fun `different queries are cached independently`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 1) } returns ricksPage1
        coEvery { remote.searchCharacters("morty", 1) } returns
            DomainResult.Success(aSearchResponseDto(aCharacterDto(2, "Morty Smith")))

        repository.searchCharacters("rick", 1)
        repository.searchCharacters("morty", 1)
        repository.searchCharacters("rick", 1)
        repository.searchCharacters("morty", 1)

        coVerify(exactly = 1) { remote.searchCharacters("rick", 1) }
        coVerify(exactly = 1) { remote.searchCharacters("morty", 1) }
    }

    @Test
    fun `failures are returned and not cached`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 1) } returnsMany listOf(
            DomainResult.Failure(DomainError.Network()),
            ricksPage1,
        )

        val failed = repository.searchCharacters("rick", 1)
        val retry = repository.searchCharacters("rick", 1).successData()

        assertEquals(DomainError.Network(), failed.failureError())
        assertEquals(listOf(1, 8), retry.characters.ids())
        coVerify(exactly = 2) { remote.searchCharacters("rick", 1) }
    }

    @Test
    fun `not found is returned and not cached`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("zzz", 1) } returns DomainResult.Failure(DomainError.NotFound)

        repeat(2) {
            assertEquals(DomainError.NotFound, repository.searchCharacters("zzz", 1).failureError())
        }

        coVerify(exactly = 2) { remote.searchCharacters("zzz", 1) }
    }

    @Test
    fun `character detail is served from search results without network`() = runTest(testDispatcher) {
        coEvery { remote.searchCharacters("rick", 1) } returns ricksPage1

        val page = repository.searchCharacters("rick", 1).successData()
        val detail = repository.getCharacter(8).successData()

        assertEquals(page.characters[1], detail)
        coVerify(exactly = 0) { remote.getCharacter(any()) }
    }

    @Test
    fun `character detail falls back to network when not cached`() = runTest(testDispatcher) {
        coEvery { remote.getCharacter(2) } returns DomainResult.Success(aCharacterDto(2, "Morty Smith"))

        val first = repository.getCharacter(2).successData()
        val second = repository.getCharacter(2).successData()

        assertEquals("Morty Smith", first.name)
        assertEquals(first, second)
        coVerify(exactly = 1) { remote.getCharacter(2) }
    }

    // endregion
}
