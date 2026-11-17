package com.example.pruebatecnica.domain.usecase

import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.fixtures.aCharacter
import com.example.pruebatecnica.domain.fixtures.failureError
import com.example.pruebatecnica.domain.fixtures.successData
import com.example.pruebatecnica.domain.repository.CharacterRepository
import com.example.pruebatecnica.domain.result.DomainResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCharacterDetailUseCaseTest {

    private val repository: CharacterRepository = mockk()
    private val useCase = GetCharacterDetailUseCase(repository)

    @Test
    fun `returns character from repository`() = runTest {
        val character = aCharacter(id = 2, name = "Morty Smith")
        coEvery { repository.getCharacter(2) } returns DomainResult.Success(character)

        val result = useCase(2)

        assertEquals(character, result.successData())
        coVerify(exactly = 1) { repository.getCharacter(2) }
    }

    @Test
    fun `not found is returned as a typed failure`() = runTest {
        coEvery { repository.getCharacter(any()) } returns DomainResult.Failure(DomainError.NotFound)

        val result = useCase(9999)

        assertEquals(DomainError.NotFound, result.failureError())
    }
}
