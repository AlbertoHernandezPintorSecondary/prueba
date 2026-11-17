package com.example.pruebatecnica.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pruebatecnica.presentation.mvi.CollectEffects

/**
 * Stateful entry point: obtains the Hilt ViewModel and connects state, intents and effects
 * to the stateless [SearchScreen]. It needs a real ViewModel, so the previews live in
 * [SearchScreen] instead.
 */
@Composable
fun SearchRoute(
    onNavigateToDetail: (characterId: Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is SearchEffect.NavigateToDetail -> onNavigateToDetail(effect.characterId)
        }
    }

    SearchScreen(state = state, onIntent = viewModel::onIntent)
}
