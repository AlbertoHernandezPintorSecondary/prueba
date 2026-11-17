package com.example.pruebatecnica.data.repository

import com.example.pruebatecnica.data.di.IoDispatcher
import com.example.pruebatecnica.data.local.CharacterLocalDataSource
import com.example.pruebatecnica.data.mapper.toDomain
import com.example.pruebatecnica.data.remote.CharacterRemoteDataSource
import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import com.example.pruebatecnica.domain.model.normalizeSearchQuery
import com.example.pruebatecnica.domain.repository.CharacterRepository
import com.example.pruebatecnica.domain.result.DomainResult
import com.example.pruebatecnica.domain.result.map
import com.example.pruebatecnica.domain.result.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first repository. It coordinates two data sources and is the boundary between
 * data and domain:
 *
 *  - [CharacterRemoteDataSource] returns API response models (DTOs); **this class maps
 *    them to domain models**, so nothing above the repository ever sees a DTO.
 *  - [CharacterLocalDataSource] stores the already-mapped domain models.
 *
 * Rules:
 *  - A valid local page is returned without hitting the network.
 *  - Otherwise (missing or expired) it is fetched remotely, mapped and saved locally.
 *  - Failures (including 404 / NotFound) are returned as [DomainResult.Failure] and never saved.
 *  - Characters from search results are also saved by id, so opening the detail screen
 *    doesn't need another request.
 *
 * All the work runs on the injected [ioDispatcher], so the repository is main-safe
 * regardless of the caller's dispatcher.
 */
@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val remoteDataSource: CharacterRemoteDataSource,
    private val localDataSource: CharacterLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CharacterRepository {

    override suspend fun searchCharacters(query: String, page: Int): DomainResult<CharacterPage> =
        withContext(ioDispatcher) {
            val normalizedQuery = normalizeSearchQuery(query)
            localDataSource.getSearchPage(normalizedQuery, page)
                ?.let { return@withContext DomainResult.Success(it) }

            remoteDataSource.searchCharacters(normalizedQuery, page)
                .map { response -> response.toDomain(currentPage = page) }
                .onSuccess { characterPage ->
                    localDataSource.saveSearchPage(normalizedQuery, page, characterPage)
                    localDataSource.saveCharacters(characterPage.characters)
                }
        }

    override suspend fun getCharacter(id: Int): DomainResult<RickAndMortyCharacter> =
        withContext(ioDispatcher) {
            localDataSource.getCharacter(id)
                ?.let { return@withContext DomainResult.Success(it) }

            remoteDataSource.getCharacter(id)
                .map { dto -> dto.toDomain() }
                .onSuccess { character -> localDataSource.saveCharacters(listOf(character)) }
        }
}
