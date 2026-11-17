package com.example.pruebatecnica.domain.usecase

import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import com.example.pruebatecnica.domain.repository.CharacterRepository
import com.example.pruebatecnica.domain.result.DomainResult
import javax.inject.Inject

/** Returns the full detail of a single character. */
class GetCharacterDetailUseCase @Inject constructor(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(id: Int): DomainResult<RickAndMortyCharacter> =
        repository.getCharacter(id)
}
