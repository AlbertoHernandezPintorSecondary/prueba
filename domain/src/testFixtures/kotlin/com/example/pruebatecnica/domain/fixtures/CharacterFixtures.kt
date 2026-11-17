package com.example.pruebatecnica.domain.fixtures

import com.example.pruebatecnica.domain.model.CharacterStatus
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter

/**
 * Shared test data. Consumed by every module's tests with
 * `testImplementation(testFixtures(project(":domain")))`.
 */
fun aCharacter(
    id: Int = 1,
    name: String = "Rick Sanchez",
    status: CharacterStatus = CharacterStatus.ALIVE,
    species: String = "Human",
    type: String = "",
    gender: String = "Male",
    originName: String = "Earth (C-137)",
    locationName: String = "Citadel of Ricks",
    episodeCount: Int = 51,
) = RickAndMortyCharacter(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    originName = originName,
    locationName = locationName,
    imageUrl = "https://rickandmortyapi.com/api/character/avatar/$id.jpeg",
    episodeCount = episodeCount,
)
