package com.example.pruebatecnica.presentation.detail

import androidx.compose.runtime.Immutable
import com.example.pruebatecnica.presentation.common.model.CharacterUi
import com.example.pruebatecnica.presentation.common.model.UiError

/** MVI contract for the detail screen. */

sealed interface DetailIntent {
    data object Retry : DetailIntent
    data object BackClicked : DetailIntent
}

@Immutable
sealed interface DetailState {
    data object Loading : DetailState
    data class Content(val character: CharacterUi) : DetailState

    /** The character does not exist (the API answers 404). */
    data object Empty : DetailState
    data class Error(val error: UiError) : DetailState
}

sealed interface DetailEffect {
    data object NavigateBack : DetailEffect
}
