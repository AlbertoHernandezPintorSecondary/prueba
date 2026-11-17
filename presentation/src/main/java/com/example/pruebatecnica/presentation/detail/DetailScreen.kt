package com.example.pruebatecnica.presentation.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.pruebatecnica.presentation.R
import com.example.pruebatecnica.presentation.common.components.EmptyState
import com.example.pruebatecnica.presentation.common.components.ErrorState
import com.example.pruebatecnica.presentation.common.components.LoadingIndicator
import com.example.pruebatecnica.presentation.common.model.UiError
import com.example.pruebatecnica.presentation.common.preview.PreviewData
import com.example.pruebatecnica.presentation.detail.components.CharacterDetailContent
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

/** Stateless detail screen: renders [DetailState] and reports user actions as [DetailIntent]s. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = (state as? DetailState.Content)?.character?.name.orEmpty()
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(DetailIntent.BackClicked) }) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (state) {
            DetailState.Loading -> LoadingIndicator(contentModifier)
            DetailState.Empty -> EmptyState(
                title = stringResource(R.string.detail_not_found_title),
                message = stringResource(R.string.detail_not_found_message),
                modifier = contentModifier,
            )
            is DetailState.Error -> ErrorState(
                error = state.error,
                onRetry = { onIntent(DetailIntent.Retry) },
                modifier = contentModifier,
            )
            is DetailState.Content -> CharacterDetailContent(
                character = state.character,
                modifier = contentModifier,
            )
        }
    }
}

private class DetailStatePreviewProvider : PreviewParameterProvider<DetailState> {
    override val values = sequenceOf(
        DetailState.Content(PreviewData.alienRick),
        DetailState.Loading,
        DetailState.Empty,
        DetailState.Error(UiError.SERVER),
    )
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview(
    @PreviewParameter(DetailStatePreviewProvider::class) state: DetailState,
) {
    PruebaTecnicaTheme {
        DetailScreen(state = state, onIntent = {})
    }
}
