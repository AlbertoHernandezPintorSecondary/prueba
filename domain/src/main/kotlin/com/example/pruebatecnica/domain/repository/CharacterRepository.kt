package com.example.pruebatecnica.domain.repository

import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import com.example.pruebatecnica.domain.result.DomainResult

/**
 * Contract the domain needs; implemented in the data layer (dependency inversion).
 *
 * Implementations must be main-safe and cooperative with coroutine cancellation.
 * Failures are returned as [DomainResult.Failure] (e.g. [com.example.pruebatecnica.domain.error.DomainError.NotFound]
 * when the API answers 404), never thrown.
 */
interface CharacterRepository {
    suspend fun searchCharacters(query: String, page: Int): DomainResult<CharacterPage>
    suspend fun getCharacter(id: Int): DomainResult<RickAndMortyCharacter>
}
