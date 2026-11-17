package com.example.pruebatecnica.presentation.common.mapper

import com.example.pruebatecnica.domain.model.CharacterStatus
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import com.example.pruebatecnica.presentation.common.model.CharacterStatusUi
import com.example.pruebatecnica.presentation.common.model.CharacterUi

/*
 * Domain -> UI model mapping functions.
 * Called by the ViewModels when the use case answers; composables only receive UI models.
 */

fun RickAndMortyCharacter.toUi(): CharacterUi = CharacterUi(
    id = id,
    name = name,
    imageUrl = imageUrl,
    status = status.toUi(),
    species = species,
    type = type.takeIf { it.isNotBlank() },
    gender = gender,
    origin = originName,
    location = locationName,
    episodeCount = episodeCount,
)

fun List<RickAndMortyCharacter>.toUi(): List<CharacterUi> = map { it.toUi() }

fun CharacterStatus.toUi(): CharacterStatusUi = when (this) {
    CharacterStatus.ALIVE -> CharacterStatusUi.ALIVE
    CharacterStatus.DEAD -> CharacterStatusUi.DEAD
    CharacterStatus.UNKNOWN -> CharacterStatusUi.UNKNOWN
}
