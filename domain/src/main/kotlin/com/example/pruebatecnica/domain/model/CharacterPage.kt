package com.example.pruebatecnica.domain.model

/**
 * One page of search results.
 *
 * @property nextPage number of the next page, or `null` when this is the last one.
 */
data class CharacterPage(
    val characters: List<RickAndMortyCharacter>,
    val nextPage: Int?,
) {
    val hasNextPage: Boolean get() = nextPage != null

    companion object {
        const val FIRST_PAGE = 1
        val EMPTY = CharacterPage(characters = emptyList(), nextPage = null)
    }
}
