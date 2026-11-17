package com.example.pruebatecnica.domain.usecase

import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.normalizeSearchQuery
import com.example.pruebatecnica.domain.repository.CharacterRepository
import com.example.pruebatecnica.domain.result.DomainResult
import javax.inject.Inject

/**
 * Searches characters by name, one page at a time.
 *
 * Business rules:
 *  - The query is normalized (trimmed + lowercased) before searching.
 *  - A blank query returns an empty page without touching the repository.
 *  - Page numbers start at [CharacterPage.FIRST_PAGE].
 */
class SearchCharactersUseCase @Inject constructor(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(
        query: String,
        page: Int = CharacterPage.FIRST_PAGE,
    ): DomainResult<CharacterPage> {
        require(page >= CharacterPage.FIRST_PAGE) { "Page must be >= ${CharacterPage.FIRST_PAGE}" }
        val normalizedQuery = normalizeSearchQuery(query)
        if (normalizedQuery.isEmpty()) return DomainResult.Success(CharacterPage.EMPTY)
        return repository.searchCharacters(normalizedQuery, page)
    }
}
