package com.example.pruebatecnica.data.local

import com.example.pruebatecnica.data.FakeTimeProvider
import com.example.pruebatecnica.data.cache.InMemoryCache
import com.example.pruebatecnica.domain.fixtures.aCharacter
import com.example.pruebatecnica.domain.model.CharacterPage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class InMemoryCharacterLocalDataSourceTest {

    private val clock = FakeTimeProvider()
    private val local = InMemoryCharacterLocalDataSource(
        searchCache = InMemoryCache(ttl = 5.minutes, timeProvider = clock),
        characterCache = InMemoryCache(ttl = 5.minutes, timeProvider = clock),
    )

    private val rick = aCharacter(1, "Rick Sanchez")
    private val morty = aCharacter(2, "Morty Smith")
    private val page = CharacterPage(listOf(rick, morty), nextPage = 2)

    @Test
    fun `saved page is returned for the same query and page only`() = runTest {
        local.saveSearchPage("rick", 1, page)

        assertEquals(page, local.getSearchPage("rick", 1))
        assertNull(local.getSearchPage("rick", 2))
        assertNull(local.getSearchPage("morty", 1))
    }

    @Test
    fun `saved page expires with the cache ttl`() = runTest {
        local.saveSearchPage("rick", 1, page)
        clock.advanceBy(5.minutes)

        assertNull(local.getSearchPage("rick", 1))
    }

    @Test
    fun `saved characters are returned by id`() = runTest {
        local.saveCharacters(listOf(rick, morty))

        assertEquals(rick, local.getCharacter(1))
        assertEquals(morty, local.getCharacter(2))
        assertNull(local.getCharacter(3))
    }
}
