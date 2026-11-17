package com.example.pruebatecnica.data.mapper

import com.example.pruebatecnica.data.remote.dto.CharacterDto
import com.example.pruebatecnica.data.remote.dto.CharactersResponseDto
import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.CharacterStatus
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter

/*
 * Response (DTO) -> domain mapping functions.
 * Used only by CharacterRepositoryImpl, which is where the mapping happens.
 */

internal fun CharacterDto.toDomain(): RickAndMortyCharacter = RickAndMortyCharacter(
    id = id,
    name = name,
    status = status.toCharacterStatus(),
    species = species,
    type = type,
    gender = gender,
    originName = origin.name,
    locationName = location.name,
    imageUrl = image,
    episodeCount = episode.size,
)

/** The API exposes the next page as a URL; the domain only needs its number. */
internal fun CharactersResponseDto.toDomain(currentPage: Int): CharacterPage = CharacterPage(
    characters = results.map { it.toDomain() },
    nextPage = if (info.next != null) currentPage + 1 else null,
)

internal fun String.toCharacterStatus(): CharacterStatus = when (lowercase()) {
    "alive" -> CharacterStatus.ALIVE
    "dead" -> CharacterStatus.DEAD
    else -> CharacterStatus.UNKNOWN
}
