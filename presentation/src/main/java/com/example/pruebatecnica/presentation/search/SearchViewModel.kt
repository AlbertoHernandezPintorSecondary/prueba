package com.example.pruebatecnica.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.normalizeSearchQuery
import com.example.pruebatecnica.domain.result.DomainResult
import com.example.pruebatecnica.domain.usecase.SearchCharactersUseCase
import com.example.pruebatecnica.presentation.common.logging.Logger
import com.example.pruebatecnica.presentation.common.mapper.toUi
import com.example.pruebatecnica.presentation.common.model.toUiError
import com.example.pruebatecnica.presentation.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Search screen ViewModel (MVI) with pagination.
 *
 * Cancellation: there is at most one [Job] alive (first page or next page). Every new
 * search cancels it before starting; since Retrofit `suspend` calls are cancellable,
 * that also cancels the in-flight HTTP request. A cancelled job can never write
 * its result into the state, so results from older searches are never shown.
 *
 * Results: the use case returns a typed [DomainResult]; every error case is handled with an
 * exhaustive `when` (404 -> empty state, the rest -> error state). Domain models are mapped
 * to UI models ([toUi]) here, as soon as the result arrives.
 *
 * Process death: the query is kept in [SavedStateHandle]; if Android kills the process,
 * the screen comes back with the same text and the first page is searched again.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchCharacters: SearchCharactersUseCase,
    private val logger: Logger,
) : BaseViewModel<SearchIntent, SearchState, SearchEffect>(
    SearchState(query = savedStateHandle.get<String>(KEY_QUERY).orEmpty()),
) {

    private var searchJob: Job? = null

    init {
        // Restored after process death: search right away (no debounce).
        if (currentState.query.isNotBlank()) searchFirstPage(currentState.query, debounce = false)
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> onQueryChanged(intent.query)
            SearchIntent.ClearQuery -> onQueryChanged("")
            SearchIntent.SubmitSearch,
            SearchIntent.Retry -> searchFirstPage(currentState.query, debounce = false)
            SearchIntent.LoadNextPage -> loadNextPage()
            is SearchIntent.CharacterClicked ->
                sendEffect(SearchEffect.NavigateToDetail(intent.characterId))
        }
    }

    private fun onQueryChanged(query: String) {
        val previousQuery = currentState.query
        if (query == previousQuery) return

        setState { copy(query = query) }
        savedStateHandle[KEY_QUERY] = query

        // "rick" -> "Rick " is the same search: keep the current results (or the search in flight)
        // instead of cancelling, flashing a spinner and asking again.
        if (normalizeSearchQuery(query) == normalizeSearchQuery(previousQuery)) return

        searchFirstPage(query, debounce = true)
    }

    private fun searchFirstPage(query: String, debounce: Boolean) {
        searchJob?.cancel()

        if (query.isBlank()) {
            setState { copy(content = SearchContent.Idle) }
            return
        }

        // Old results are cleared straight away: the list never shows data for another query.
        setState { copy(content = SearchContent.Loading) }

        searchJob = viewModelScope.launch {
            if (debounce) delay(DEBOUNCE_MILLIS)

            val result = searchCharacters(query, CharacterPage.FIRST_PAGE)

            ensureActive() // defensive: never publish the result of a cancelled search
            logIfFailed(result, "Search failed (query=\"$query\", page=${CharacterPage.FIRST_PAGE})")
            val content = firstPageContent(query, result)
            setState { copy(content = content) }
        }
    }

    private fun loadNextPage() {
        val results = currentState.content as? SearchContent.Results ?: return
        val nextPage = results.nextPage ?: return
        if (results.isLoadingMore || searchJob?.isActive == true) return

        val query = currentState.query
        setState { copy(content = results.copy(isLoadingMore = true, loadMoreError = null)) }

        searchJob = viewModelScope.launch {
            val result = searchCharacters(query, nextPage)

            ensureActive()
            logIfFailed(result, "Load more failed (query=\"$query\", page=$nextPage)")
            setState {
                val current = content as? SearchContent.Results ?: return@setState this
                copy(content = current.appendPage(result))
            }
        }
    }

    /** First page result -> screen content. */
    private fun firstPageContent(query: String, result: DomainResult<CharacterPage>): SearchContent =
        when (result) {
            is DomainResult.Success ->
                if (result.data.characters.isEmpty()) SearchContent.Empty(query.trim())
                else SearchContent.Results(result.data.characters.toUi(), result.data.nextPage)

            is DomainResult.Failure -> when (val error = result.error) {
                // 404 from the API means "no matches" -> empty state, not an error.
                DomainError.NotFound -> SearchContent.Empty(query.trim())
                is DomainError.Network,
                is DomainError.Server,
                is DomainError.Unknown -> SearchContent.Error(error.toUiError())
            }
        }

    /** Next page result -> updated results (appended, end reached, or inline error). */
    private fun SearchContent.Results.appendPage(result: DomainResult<CharacterPage>): SearchContent.Results =
        when (result) {
            is DomainResult.Success -> copy(
                characters = (characters + result.data.characters.toUi()).distinctBy { it.id },
                nextPage = result.data.nextPage,
                isLoadingMore = false,
            )

            is DomainResult.Failure -> when (val error = result.error) {
                // Nothing beyond this page: stop paginating.
                DomainError.NotFound -> copy(nextPage = null, isLoadingMore = false)
                is DomainError.Network,
                is DomainError.Server,
                is DomainError.Unknown -> copy(isLoadingMore = false, loadMoreError = error.toUiError())
            }
        }

    /** Logs real failures (404 is an expected "no results", not a failure). */
    private fun logIfFailed(result: DomainResult<*>, message: String) {
        val error = (result as? DomainResult.Failure)?.error ?: return
        if (error != DomainError.NotFound) logger.warn(TAG, "$message: $error", error.cause)
    }

    companion object {
        const val DEBOUNCE_MILLIS = 400L
        internal const val KEY_QUERY = "search_query"
        internal const val TAG = "SearchViewModel"
    }
}
