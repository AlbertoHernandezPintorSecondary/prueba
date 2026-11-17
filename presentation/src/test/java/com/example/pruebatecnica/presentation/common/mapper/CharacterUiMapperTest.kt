package com.example.pruebatecnica.presentation.common.mapper

import com.example.pruebatecnica.domain.fixtures.aCharacter
import com.example.pruebatecnica.domain.model.CharacterStatus
import com.example.pruebatecnica.presentation.common.model.CharacterStatusUi
import com.example.pruebatecnica.presentation.common.model.CharacterUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterUiMapperTest {

    private val alienRick = aCharacter(
        id = 15,
        name = "Alien Rick",
        status = CharacterStatus.UNKNOWN,
        species = "Alien",
        type = "Parasite",
        originName = "Unknown dimension",
        episodeCount = 1,
    )

    @Test
    fun `maps every field to the ui model`() {
        assertEquals(
            CharacterUi(
                id = 15,
                name = "Alien Rick",
                imageUrl = "https://rickandmortyapi.com/api/character/avatar/15.jpeg",
                status = CharacterStatusUi.UNKNOWN,
                species = "Alien",
                type = "Parasite",
                gender = "Male",
                origin = "Unknown dimension",
                location = "Citadel of Ricks",
                episodeCount = 1,
            ),
            alienRick.toUi(),
        )
    }

    @Test
    fun `blank type becomes null so the row is hidden`() {
        assertNull(alienRick.copy(type = "  ").toUi().type)
    }

    @Test
    fun `maps every status`() {
        assertEquals(CharacterStatusUi.ALIVE, CharacterStatus.ALIVE.toUi())
        assertEquals(CharacterStatusUi.DEAD, CharacterStatus.DEAD.toUi())
        assertEquals(CharacterStatusUi.UNKNOWN, CharacterStatus.UNKNOWN.toUi())
    }

    @Test
    fun `maps lists keeping the order`() {
        val morty = alienRick.copy(id = 2, name = "Morty Smith")

        assertEquals(listOf(15, 2), listOf(alienRick, morty).toUi().map { it.id })
    }
}
