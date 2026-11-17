package com.example.pruebatecnica.domain.usecase

import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.fixtures.aCharacter
import com.example.pruebatecnica.domain.fixtures.failureError
import com.example.pruebatecnica.domain.fixtures.successData
import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.repository.CharacterRepository
import com.example.pruebatecnica.domain.result.DomainResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class SearchCharactersUseCaseTest {

    private lateinit var repository: CharacterRepository
    private lateinit var useCase: SearchCharactersUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SearchCharactersUseCase(repository)
    }

    @Test
    fun `returns the first page from repository by default`() = runTest {
        val page = CharacterPage(listOf(aCharacter(1), aCharacter(2, "Rick Prime")), nextPage = 2)
        coEvery { repository.searchCharacters("rick", 1) } returns DomainResult.Success(page)

        val result = useCase("rick")

        assertEquals(page, result.successData())
        coVerify(exactly = 1) { repository.searchCharacters("rick", 1) }
    }

    @Test
    fun `requests the given page`() = runTest {
        val page = CharacterPage(listOf(aCharacter(21)), nextPage = null)
        coEvery { repository.searchCharacters("rick", 2) } returns DomainResult.Success(page)

        val result = useCase("rick", page = 2)

        assertEquals(page, result.successData())
        coVerify(exactly = 1) { repository.searchCharacters("rick", 2) }
    }

    @Test
    fun `normalizes the query before calling repository`() = runTest {
        coEvery { repository.searchCharacters("morty", 1) } returns DomainResult.Success(CharacterPage.EMPTY)

        useCase("   MoRty  ")

        coVerify(exactly = 1) { repository.searchCharacters("morty", 1) }
    }

    @Test
    fun `blank query returns empty page without calling repository`() = runTest {
        val result = useCase("   ")

        assertEquals(CharacterPage.EMPTY, result.successData())
        coVerify(exactly = 0) { repository.searchCharacters(any(), any()) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid page number is rejected`() = runTest {
        useCase("rick", page = 0)
    }

    @Test
    fun `not found is returned as a typed failure`() = runTest {
        coEvery { repository.searchCharacters(any(), any()) } returns DomainResult.Failure(DomainError.NotFound)

        val result = useCase("zzz")

        assertEquals(DomainError.NotFound, result.failureError())
    }

    @Test
    fun `other errors are returned as typed failures`() = runTest {
        coEvery { repository.searchCharacters(any(), any()) } returns DomainResult.Failure(DomainError.Server(500))

        val result = useCase("rick")

        assertEquals(DomainError.Server(500), result.failureError())
    }

    @Test
    fun `cancellation is propagated, never turned into a failure`() = runTest {
        coEvery { repository.searchCharacters(any(), any()) } throws CancellationException("cancelled")

        try {
            useCase("rick")
            fail("CancellationException should have been propagated")
        } catch (e: CancellationException) {
            // expected
        }
    }
}
