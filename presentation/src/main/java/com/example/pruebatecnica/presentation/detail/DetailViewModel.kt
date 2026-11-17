package com.example.pruebatecnica.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.result.DomainResult
import com.example.pruebatecnica.domain.usecase.GetCharacterDetailUseCase
import com.example.pruebatecnica.presentation.common.logging.Logger
import com.example.pruebatecnica.presentation.common.mapper.toUi
import com.example.pruebatecnica.presentation.common.model.toUiError
import com.example.pruebatecnica.presentation.mvi.BaseViewModel
import com.example.pruebatecnica.presentation.navigation.DetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCharacterDetail: GetCharacterDetailUseCase,
    private val logger: Logger,
) : BaseViewModel<DetailIntent, DetailState, DetailEffect>(DetailState.Loading) {

    /** Navigation argument, read from the type-safe [DetailDestination] route. */
    private val characterId: Int = checkNotNull(savedStateHandle.get<Int>(DetailDestination::characterId.name)) {
        "Missing ${DetailDestination::characterId.name} navigation argument"
    }

    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onIntent(intent: DetailIntent) {
        when (intent) {
            DetailIntent.Retry -> load()
            DetailIntent.BackClicked -> sendEffect(DetailEffect.NavigateBack)
        }
    }

    private fun load() {
        loadJob?.cancel()
        setState { DetailState.Loading }
        loadJob = viewModelScope.launch {
            val newState = when (val result = getCharacterDetail(characterId)) {
                // Domain -> UI model mapping happens here, as soon as the use case answers.
                is DomainResult.Success -> DetailState.Content(result.data.toUi())

                is DomainResult.Failure -> when (val error = result.error) {
                    // 404: the character does not exist -> empty state.
                    DomainError.NotFound -> DetailState.Empty
                    is DomainError.Network,
                    is DomainError.Server,
                    is DomainError.Unknown -> {
                        logger.warn(TAG, "Loading character $characterId failed: $error", error.cause)
                        DetailState.Error(error.toUiError())
                    }
                }
            }
            setState { newState }
        }
    }

    internal companion object {
        const val TAG = "DetailViewModel"
    }
}
