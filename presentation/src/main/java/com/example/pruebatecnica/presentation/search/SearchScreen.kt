package com.example.pruebatecnica.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.pruebatecnica.presentation.R
import com.example.pruebatecnica.presentation.common.components.EmptyState
import com.example.pruebatecnica.presentation.common.components.ErrorState
import com.example.pruebatecnica.presentation.common.components.LoadingIndicator
import com.example.pruebatecnica.presentation.common.model.UiError
import com.example.pruebatecnica.presentation.common.preview.PreviewData
import com.example.pruebatecnica.presentation.search.components.CharacterList
import com.example.pruebatecnica.presentation.search.components.SearchField
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

/** Stateless search screen: renders [SearchState] and reports user actions as [SearchIntent]s. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.search_title)) }) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = state.query,
                onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                onClear = { onIntent(SearchIntent.ClearQuery) },
                onSearch = { onIntent(SearchIntent.SubmitSearch) },
            )
            Box(Modifier.fillMaxSize()) {
                when (val content = state.content) {
                    SearchContent.Idle -> EmptyState(
                        title = stringResource(R.string.search_idle_title),
                        message = stringResource(R.string.search_idle_message),
                    )
                    SearchContent.Loading -> LoadingIndicator()
                    is SearchContent.Empty -> EmptyState(
                        title = stringResource(R.string.search_empty_title),
                        message = stringResource(R.string.search_empty_message, content.query),
                    )
                    is SearchContent.Error -> ErrorState(
                        error = content.error,
                        onRetry = { onIntent(SearchIntent.Retry) },
                    )
                    is SearchContent.Results -> CharacterList(
                        characters = content.characters,
                        canLoadMore = content.canLoadMore,
                        isLoadingMore = content.isLoadingMore,
                        loadMoreError = content.loadMoreError,
                        onCharacterClick = { onIntent(SearchIntent.CharacterClicked(it)) },
                        onLoadMore = { onIntent(SearchIntent.LoadNextPage) },
                    )
                }
            }
        }
    }
}

private class SearchStatePreviewProvider : PreviewParameterProvider<SearchState> {
    override val values = sequenceOf(
        SearchState(),
        SearchState(query = "rick", content = SearchContent.Loading),
        SearchState(query = "rick", content = SearchContent.Results(PreviewData.characters, nextPage = 2)),
        SearchState(
            query = "rick",
            content = SearchContent.Results(PreviewData.characters, nextPage = 2, isLoadingMore = true),
        ),
        SearchState(query = "jerryboree", content = SearchContent.Empty("jerryboree")),
        SearchState(query = "rick", content = SearchContent.Error(UiError.NETWORK)),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(SearchStatePreviewProvider::class) state: SearchState,
) {
    PruebaTecnicaTheme {
        SearchScreen(state = state, onIntent = {})
    }
}
