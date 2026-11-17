package com.example.pruebatecnica.presentation.search

import androidx.lifecycle.SavedStateHandle
import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.usecase.SearchCharactersUseCase
import com.example.pruebatecnica.presentation.FakeCharacterRepository
import com.example.pruebatecnica.presentation.FakeCharacterRepository.SearchCall
import com.example.pruebatecnica.presentation.FakeLogger
import com.example.pruebatecnica.presentation.MainDispatcherRule
import com.example.pruebatecnica.domain.fixtures.aCharacter
import com.example.pruebatecnica.presentation.common.mapper.toUi
import com.example.pruebatecnica.presentation.common.model.UiError
import com.example.pruebatecnica.presentation.search.SearchViewModel.Companion.DEBOUNCE_MILLIS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCharacterRepository()
    private val logger = FakeLogger()
    private val ricksPage1 = listOf(aCharacter(1, "Rick Sanchez"), aCharacter(8, "Adjudicator Rick"))
    private val ricksPage2 = listOf(aCharacter(15, "Alien Rick"))
    private val mortys = listOf(aCharacter(2, "Morty Smith"))

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        SearchViewModel(savedStateHandle, SearchCharactersUseCase(repository), logger)

    private val SearchViewModel.content get() = state.value.content

    // region first page

    @Test
    fun `initial state is idle with empty query`() {
        val viewModel = createViewModel()

        assertEquals(SearchState(query = "", content = SearchContent.Idle), viewModel.state.value)
    }

    @Test
    fun `typing shows loading and searches after debounce`() = runTest {
        repository.givenSearch("rick", ricksPage1)
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("rick"))

        assertEquals("rick", viewModel.state.value.query)
        assertEquals(SearchContent.Loading, viewModel.content)

        advanceTimeBy(DEBOUNCE_MILLIS - 1)
        assertTrue("No request before debounce elapses", repository.searchCalls.isEmpty())

        advanceUntilIdle()
        assertEquals(SearchContent.Results(ricksPage1.toUi(), nextPage = null), viewModel.content)
    }

    @Test
    fun `typed text is sent normalized`() = runTest {
        repository.givenSearch("rick", ricksPage1)
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("  RiCK "))
        advanceUntilIdle()

        assertEquals(listOf(SearchCall("rick", 1)), repository.searchCalls)
    }

    @Test
    fun `typing quickly only searches the last query`() = runTest {
        repository.givenSearch("rick", ricksPage1)
        val viewModel = createViewModel()

        listOf("r", "ri", "ric", "rick").forEach {
            viewModel.onIntent(SearchIntent.QueryChanged(it))
            advanceTimeBy(DEBOUNCE_MILLIS / 4)
        }
        advanceUntilIdle()

        assertEquals(listOf(SearchCall("rick", 1)), repository.searchCalls)
    }

    @Test
    fun `new search cancels the in-flight one and its results are never shown`() = runTest {
        repository.givenSearch("rick", ricksPage1, delayMillis = 1_000)
        repository.givenSearch("morty", mortys, delayMillis = 100)
        val viewModel = createViewModel()
        val emitted = mutableListOf<SearchContent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { emitted += it.content }
        }

        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceTimeBy(DEBOUNCE_MILLIS + 500) // "rick" request is in flight
        assertEquals(listOf(SearchCall("rick", 1)), repository.searchCalls)

        viewModel.onIntent(SearchIntent.QueryChanged("morty"))
        advanceUntilIdle()

        assertEquals(listOf(SearchCall("rick", 1)), repository.cancelledSearches)
        assertEquals(SearchContent.Results(mortys.toUi(), nextPage = null), viewModel.content)
        assertFalse(
            "Stale results from the cancelled search must never be emitted",
            emitted.any { it is SearchContent.Results && it.characters == ricksPage1.toUi() },
        )
    }

    @Test
    fun `submit search skips the debounce`() = runTest {
        repository.givenSearch("rick", ricksPage1)
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        viewModel.onIntent(SearchIntent.SubmitSearch)
        runCurrent()

        assertEquals(SearchContent.Results(ricksPage1.toUi(), nextPage = null), viewModel.content)
        advanceUntilIdle()
        assertEquals("The debounced search was cancelled", 1, repository.searchCalls.size)
    }

    @Test
    fun `same normalized query does not search again`() = runTest {
        repository.givenSearch("rick", ricksPage1)
        val viewModel = createViewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.QueryChanged("Rick "))
        assertEquals("No spinner flash", SearchContent.Results(ricksPage1.toUi(), nextPage = null), viewModel.content)
        advanceUntilIdle()

        assertEquals("Rick ", viewModel.state.value.query)
        assertEquals(listOf(SearchCall("rick", 1)), repository.searchCalls)
    }

    @Test
    fun `same normalized query keeps the search in flight instead of restarting it`() = runTest {
        repository.givenSearch("rick", ricksPage1, delayMillis = 1_000)
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceTimeBy(DEBOUNCE_MILLIS + 500)
        viewModel.onIntent(SearchIntent.QueryChanged("RICK"))
        advanceUntilIdle()

        assertTrue(repository.cancelledSearches.isEmpty())
        assertEquals(SearchContent.Results(ricksPage1.toUi(), nextPage = null), viewModel.content)
    }

    // endregion

    // region process death (SavedStateHandle)

    @Test
    fun `query is saved in SavedStateHandle`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)

        viewModel.onIntent(SearchIntent.QueryChanged("morty"))

        assertEquals("morty", savedStateHandle.get<String>(SearchViewModel.KEY_QUERY))
    }

    @Test
    fun `restored query is searched again right away`() = runTest {
        repository.givenSearch("morty", mortys)

        val viewModel = createViewModel(SavedStateHandle(mapOf(SearchViewModel.KEY_QUERY to "morty")))

        assertEquals("morty", viewModel.state.value.query)
        assertEquals(SearchContent.Loading, viewModel.content)
        runCurrent() // no debounce on restore
        assertEquals(SearchContent.Results(mortys.toUi(), nextPage = null), viewModel.content)
    }

    // endregion

    // region empty / error

    @Test
    fun `404 from the api shows empty state`() = runTest {
        repository.givenSearchError("zzz", DomainError.NotFound)
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("zzz"))
        advanceUntilIdle()

        assertEquals(SearchContent.Empty("zzz"), viewModel.content)
        assertTrue("404 is an expected 'no results', not logged", logger.warnings.isEmpty())
    }

    @Test
    fun `empty page shows empty state`() = runTest {
        repository.givenSearch("zzz", emptyList())
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("zzz"))
        advanceUntilIdle()

        assertEquals(SearchContent.Empty("zzz"), viewModel.content)
    }

    @Test
    fun `error shows error state and retry recovers`() = runTest {
        repository.givenSearchError("rick", DomainError.Network())
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()
        assertEquals(SearchContent.Error(UiError.NETWORK), viewModel.content)

        repository.givenSearch("rick", ricksPage1)
        viewModel.onIntent(SearchIntent.Retry)
        advanceUntilIdle()
        assertEquals(SearchContent.Results(ricksPage1.toUi(), nextPage = null), viewModel.content)
    }

    @Test
    fun `each error type maps to its ui error`() = runTest {
        val cases = mapOf(
            "network" to (DomainError.Network() to UiError.NETWORK),
            "server" to (DomainError.Server(500) to UiError.SERVER),
            "unknown" to (DomainError.Unknown() to UiError.UNKNOWN),
        )
        cases.forEach { (query, errorToUi) -> repository.givenSearchError(query, errorToUi.first) }
        val viewModel = createViewModel()

        cases.forEach { (query, errorToUi) ->
            viewModel.onIntent(SearchIntent.QueryChanged(query))
            advanceUntilIdle()
            assertEquals(SearchContent.Error(errorToUi.second), viewModel.content)
        }
    }

    @Test
    fun `failures are logged with the original cause`() = runTest {
        val cause = java.io.IOException("timeout")
        repository.givenSearchError("rick", DomainError.Network(cause))
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()

        val warning = logger.warnings.single()
        assertEquals(SearchViewModel.TAG, warning.tag)
        assertEquals(cause, warning.throwable)
    }

    @Test
    fun `clearing the query cancels the search and goes back to idle`() = runTest {
        repository.givenSearch("rick", ricksPage1, delayMillis = 1_000)
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceTimeBy(DEBOUNCE_MILLIS + 500)
        viewModel.onIntent(SearchIntent.ClearQuery)
        advanceUntilIdle()

        assertEquals(SearchState(query = "", content = SearchContent.Idle), viewModel.state.value)
        assertEquals(listOf(SearchCall("rick", 1)), repository.cancelledSearches)
    }

    // endregion

    // region pagination

    @Test
    fun `load next page appends results`() = runTest {
        repository.givenSearch("rick", ricksPage1, page = 1, nextPage = 2)
        repository.givenSearch("rick", ricksPage2, page = 2, nextPage = null)
        val viewModel = createViewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.LoadNextPage)
        val loading = viewModel.content as SearchContent.Results
        assertTrue(loading.isLoadingMore)

        advanceUntilIdle()
        val results = viewModel.content as SearchContent.Results
        assertEquals((ricksPage1 + ricksPage2).toUi(), results.characters)
        assertFalse(results.isLoadingMore)
        assertFalse(results.canLoadMore)
    }

    @Test
    fun `load next page does nothing on the last page`() = runTest {
        repository.givenSearch("rick", ricksPage1, page = 1, nextPage = null)
        val viewModel = createViewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.LoadNextPage)
        advanceUntilIdle()

        assertEquals(listOf(SearchCall("rick", 1)), repository.searchCalls)
    }

    @Test
    fun `duplicated load next page requests only fetch once`() = runTest {
        repository.givenSearch("rick", ricksPage1, page = 1, nextPage = 2)
        repository.givenSearch("rick", ricksPage2, page = 2, delayMillis = 500)
        val viewModel = createViewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()

        repeat(3) { viewModel.onIntent(SearchIntent.LoadNextPage) }
        advanceUntilIdle()

        assertEquals(1, repository.searchCalls.count { it.page == 2 })
    }

    @Test
    fun `load more error keeps results and retry loads the page`() = runTest {
        repository.givenSearch("rick", ricksPage1, page = 1, nextPage = 2)
        repository.givenSearchError("rick", DomainError.Network(), page = 2)
        val viewModel = createViewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.LoadNextPage)
        advanceUntilIdle()
        val failed = viewModel.content as SearchContent.Results
        assertEquals(ricksPage1.toUi(), failed.characters)
        assertEquals(UiError.NETWORK, failed.loadMoreError)

        repository.givenSearch("rick", ricksPage2, page = 2)
        viewModel.onIntent(SearchIntent.LoadNextPage)
        advanceUntilIdle()
        val recovered = viewModel.content as SearchContent.Results
        assertEquals((ricksPage1 + ricksPage2).toUi(), recovered.characters)
        assertEquals(null, recovered.loadMoreError)
    }

    @Test
    fun `new query cancels an in-flight next page`() = runTest {
        repository.givenSearch("rick", ricksPage1, page = 1, nextPage = 2)
        repository.givenSearch("rick", ricksPage2, page = 2, delayMillis = 1_000)
        repository.givenSearch("morty", mortys)
        val viewModel = createViewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("rick"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.LoadNextPage)
        advanceTimeBy(500)
        viewModel.onIntent(SearchIntent.QueryChanged("morty"))
        advanceUntilIdle()

        assertEquals(listOf(SearchCall("rick", 2)), repository.cancelledSearches)
        assertEquals(SearchContent.Results(mortys.toUi(), nextPage = null), viewModel.content)
    }

    // endregion

    @Test
    fun `clicking a character emits a navigation effect`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(SearchIntent.CharacterClicked(8))

        assertEquals(SearchEffect.NavigateToDetail(8), viewModel.effects.first())
    }
}
