package com.example.pruebatecnica.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pruebatecnica.presentation.mvi.CollectEffects

/**
 * Stateful entry point: obtains the Hilt ViewModel and connects state, intents and effects
 * to the stateless [DetailScreen]. It needs a real ViewModel, so the previews live in
 * [DetailScreen] instead.
 */
@Composable
fun DetailRoute(
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            DetailEffect.NavigateBack -> onNavigateBack()
        }
    }

    DetailScreen(state = state, onIntent = viewModel::onIntent)
}
