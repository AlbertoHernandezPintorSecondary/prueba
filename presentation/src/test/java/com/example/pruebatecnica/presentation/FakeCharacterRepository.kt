package com.example.pruebatecnica.presentation

import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import com.example.pruebatecnica.domain.repository.CharacterRepository
import com.example.pruebatecnica.domain.result.DomainResult
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

/**
 * Fake repository with configurable latency per (query, page). Records which searches
 * were started and which were cancelled, so tests can assert on cancellation.
 * Unconfigured searches answer 404 (NotFound), like the real API.
 */
class FakeCharacterRepository : CharacterRepository {

    data class SearchCall(val query: String, val page: Int)

    private data class SearchResponse(val delayMillis: Long, val result: DomainResult<CharacterPage>)

    private val searchResponses = mutableMapOf<SearchCall, SearchResponse>()
    private val detailResponses = ArrayDeque<DomainResult<RickAndMortyCharacter>>()

    val searchCalls = mutableListOf<SearchCall>()
    val cancelledSearches = mutableListOf<SearchCall>()

    fun givenSearch(
        query: String,
        characters: List<RickAndMortyCharacter>,
        page: Int = 1,
        nextPage: Int? = null,
        delayMillis: Long = 0,
    ) {
        searchResponses[SearchCall(query, page)] =
            SearchResponse(delayMillis, DomainResult.Success(CharacterPage(characters, nextPage)))
    }

    fun givenSearchError(query: String, error: DomainError, page: Int = 1, delayMillis: Long = 0) {
        searchResponses[SearchCall(query, page)] = SearchResponse(delayMillis, DomainResult.Failure(error))
    }

    fun givenDetail(vararg results: DomainResult<RickAndMortyCharacter>) {
        detailResponses.addAll(results)
    }

    override suspend fun searchCharacters(query: String, page: Int): DomainResult<CharacterPage> {
        val call = SearchCall(query, page)
        searchCalls += call
        val response = searchResponses[call]
            ?: SearchResponse(0, DomainResult.Failure(DomainError.NotFound))
        try {
            delay(response.delayMillis)
        } catch (e: CancellationException) {
            cancelledSearches += call
            throw e
        }
        return response.result
    }

    override suspend fun getCharacter(id: Int): DomainResult<RickAndMortyCharacter> =
        detailResponses.removeFirst()
}
