package com.example.pruebatecnica.data.local

import com.example.pruebatecnica.data.cache.InMemoryCache
import com.example.pruebatecnica.data.cache.SearchCacheKey
import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local source of characters (already mapped to domain models).
 *
 * The repository only coordinates remote + local through these two abstractions, so the
 * in-memory implementation could be replaced by a persistent one (e.g. Room) without
 * touching the repository. Functions are `suspend` so a disk-based implementation fits.
 */
interface CharacterLocalDataSource {
    /** Returns the cached page, or `null` if missing or expired. */
    suspend fun getSearchPage(query: String, page: Int): CharacterPage?
    suspend fun saveSearchPage(query: String, page: Int, characterPage: CharacterPage)

    /** Returns the cached character, or `null` if missing or expired. */
    suspend fun getCharacter(id: Int): RickAndMortyCharacter?
    suspend fun saveCharacters(characters: List<RickAndMortyCharacter>)
}

/** In-memory implementation backed by two TTL + LRU caches. */
@Singleton
class InMemoryCharacterLocalDataSource @Inject constructor(
    private val searchCache: InMemoryCache<SearchCacheKey, CharacterPage>,
    private val characterCache: InMemoryCache<Int, RickAndMortyCharacter>,
) : CharacterLocalDataSource {

    override suspend fun getSearchPage(query: String, page: Int): CharacterPage? =
        searchCache.get(SearchCacheKey(query, page))

    override suspend fun saveSearchPage(query: String, page: Int, characterPage: CharacterPage) {
        searchCache.put(SearchCacheKey(query, page), characterPage)
    }

    override suspend fun getCharacter(id: Int): RickAndMortyCharacter? =
        characterCache.get(id)

    override suspend fun saveCharacters(characters: List<RickAndMortyCharacter>) {
        characters.forEach { characterCache.put(it.id, it) }
    }
}
