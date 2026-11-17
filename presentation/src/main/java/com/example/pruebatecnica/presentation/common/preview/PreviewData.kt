package com.example.pruebatecnica.presentation.common.preview

import com.example.pruebatecnica.presentation.common.model.CharacterStatusUi
import com.example.pruebatecnica.presentation.common.model.CharacterUi

/** Sample data shared by every @Preview. */
object PreviewData {

    val rick = CharacterUi(
        id = 1,
        name = "Rick Sanchez",
        imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
        status = CharacterStatusUi.ALIVE,
        species = "Human",
        type = null,
        gender = "Male",
        origin = "Earth (C-137)",
        location = "Citadel of Ricks",
        episodeCount = 51,
    )

    val adjudicatorRick = CharacterUi(
        id = 8,
        name = "Adjudicator Rick",
        imageUrl = "https://rickandmortyapi.com/api/character/avatar/8.jpeg",
        status = CharacterStatusUi.DEAD,
        species = "Human",
        type = null,
        gender = "Male",
        origin = "unknown",
        location = "Citadel of Ricks",
        episodeCount = 1,
    )

    val alienRick = CharacterUi(
        id = 15,
        name = "Alien Rick",
        imageUrl = "https://rickandmortyapi.com/api/character/avatar/15.jpeg",
        status = CharacterStatusUi.UNKNOWN,
        species = "Alien",
        type = "Parasite",
        gender = "Male",
        origin = "Unknown dimension",
        location = "Earth (Replacement Dimension)",
        episodeCount = 1,
    )

    val characters = listOf(rick, adjudicatorRick, alienRick)
}
