package com.example.pruebatecnica.presentation.search

import androidx.compose.runtime.Immutable
import com.example.pruebatecnica.presentation.common.model.CharacterUi
import com.example.pruebatecnica.presentation.common.model.UiError

/** MVI contract for the search screen. */

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object SubmitSearch : SearchIntent
    data object ClearQuery : SearchIntent
    data object Retry : SearchIntent
    /** Requests the next page (sent by the list when scrolling near the end, or by "retry" in the footer). */
    data object LoadNextPage : SearchIntent
    data class CharacterClicked(val characterId: Int) : SearchIntent
}

@Immutable
data class SearchState(
    val query: String = "",
    val content: SearchContent = SearchContent.Idle,
)

@Immutable
sealed interface SearchContent {
    data object Idle : SearchContent
    data object Loading : SearchContent

    /** No matches (the API answers 404). */
    data class Empty(val query: String) : SearchContent
    data class Error(val error: UiError) : SearchContent

    data class Results(
        val characters: List<CharacterUi>,
        val nextPage: Int?,
        val isLoadingMore: Boolean = false,
        val loadMoreError: UiError? = null,
    ) : SearchContent {
        val canLoadMore: Boolean get() = nextPage != null
    }
}

sealed interface SearchEffect {
    data class NavigateToDetail(val characterId: Int) : SearchEffect
}
