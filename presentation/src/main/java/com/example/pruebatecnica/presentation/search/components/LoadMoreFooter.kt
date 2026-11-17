package com.example.pruebatecnica.presentation.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebatecnica.presentation.R
import com.example.pruebatecnica.presentation.common.model.UiError
import com.example.pruebatecnica.presentation.common.model.messageRes
import com.example.pruebatecnica.presentation.theme.PruebaTecnicaTheme

/**
 * Last list item while paginating: a small spinner, or an inline error that says
 * why the page failed (no connection, server error...) with a retry button.
 */
@Composable
fun LoadMoreFooter(
    isLoading: Boolean,
    error: UiError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            isLoading -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            error != null -> {
                Text(
                    text = stringResource(R.string.search_load_more_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(error.messageRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadMoreFooterPreview() {
    PruebaTecnicaTheme {
        Column {
            LoadMoreFooter(isLoading = true, error = null, onRetry = {})
            LoadMoreFooter(isLoading = false, error = UiError.NETWORK, onRetry = {})
            LoadMoreFooter(isLoading = false, error = UiError.SERVER, onRetry = {})
        }
    }
}
