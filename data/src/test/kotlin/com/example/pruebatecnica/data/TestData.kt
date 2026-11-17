package com.example.pruebatecnica.data

import com.example.pruebatecnica.data.remote.dto.CharacterDto
import com.example.pruebatecnica.data.remote.dto.CharactersResponseDto
import com.example.pruebatecnica.data.remote.dto.NamedLinkDto
import com.example.pruebatecnica.data.remote.dto.PageInfoDto

/** API response model for a character, as the remote data source returns it. */
fun aCharacterDto(
    id: Int = 1,
    name: String = "Rick Sanchez",
    status: String = "Alive",
    type: String = "",
    episodes: Int = 51,
) = CharacterDto(
    id = id,
    name = name,
    status = status,
    species = "Human",
    type = type,
    gender = "Male",
    origin = NamedLinkDto(name = "Earth (C-137)", url = "https://rickandmortyapi.com/api/location/1"),
    location = NamedLinkDto(name = "Citadel of Ricks", url = "https://rickandmortyapi.com/api/location/3"),
    image = "https://rickandmortyapi.com/api/character/avatar/$id.jpeg",
    episode = List(episodes) { "https://rickandmortyapi.com/api/episode/${it + 1}" },
)

/** API search response. [hasNext] controls whether `info.next` points to another page. */
fun aSearchResponseDto(
    vararg characters: CharacterDto,
    hasNext: Boolean = false,
) = CharactersResponseDto(
    info = PageInfoDto(
        count = characters.size,
        pages = if (hasNext) 2 else 1,
        next = if (hasNext) "https://rickandmortyapi.com/api/character/?page=2" else null,
    ),
    results = characters.toList(),
)
