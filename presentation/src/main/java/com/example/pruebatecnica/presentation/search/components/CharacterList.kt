package com.example.pruebatecnica.presentation.search.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebatecnica.presentation.common.model.CharacterUi
import com.example.pruebatecnica.presentation.common.model.UiError
import com.example.pruebatecnica.presentation.common.preview.PreviewData
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

/** How many items before the end of the list the next page is requested. */
private const val LOAD_MORE_THRESHOLD = 5

/** Different content types let LazyColumn reuse compositions only between items of the same kind. */
private const val CONTENT_TYPE_CHARACTER = "character"
private const val CONTENT_TYPE_FOOTER = "load_more_footer"
private const val KEY_FOOTER = "load_more_footer"

/**
 * Paginated list (infinite scroll). When the user gets within [LOAD_MORE_THRESHOLD]
 * items of the end, [onLoadMore] is called. After a load-more error, auto-loading
 * stops until the user taps "retry" in the footer.
 */
@Composable
fun CharacterList(
    characters: List<CharacterUi>,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    loadMoreError: UiError?,
    onCharacterClick: (characterId: Int) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val isNearEnd by remember(listState) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            lastVisibleIndex >= listState.layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    val autoLoadEnabled = canLoadMore && !isLoadingMore && loadMoreError == null

    LaunchedEffect(isNearEnd, autoLoadEnabled) {
        if (isNearEnd && autoLoadEnabled) currentOnLoadMore()
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(
            items = characters,
            key = { it.id },
            contentType = { CONTENT_TYPE_CHARACTER },
        ) { character ->
            CharacterListItem(
                character = character,
                onClick = { onCharacterClick(character.id) },
            )
        }
        if (isLoadingMore || loadMoreError != null) {
            item(key = KEY_FOOTER, contentType = CONTENT_TYPE_FOOTER) {
                LoadMoreFooter(
                    isLoading = isLoadingMore,
                    error = loadMoreError,
                    onRetry = onLoadMore,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterListPreview() {
    PruebaTecnicaTheme {
        CharacterList(
            characters = PreviewData.characters,
            canLoadMore = true,
            isLoadingMore = true,
            loadMoreError = null,
            onCharacterClick = {},
            onLoadMore = {},
        )
    }
}
