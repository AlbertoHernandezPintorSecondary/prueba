package com.example.pruebatecnica.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.fixtures.aCharacter
import com.example.pruebatecnica.domain.result.DomainResult
import com.example.pruebatecnica.domain.usecase.GetCharacterDetailUseCase
import com.example.pruebatecnica.presentation.FakeCharacterRepository
import com.example.pruebatecnica.presentation.FakeLogger
import com.example.pruebatecnica.presentation.MainDispatcherRule
import com.example.pruebatecnica.presentation.common.mapper.toUi
import com.example.pruebatecnica.presentation.common.model.UiError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCharacterRepository()
    private val logger = FakeLogger()
    private val morty = aCharacter(2, "Morty Smith")

    private fun createViewModel(id: Int = 2) = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("characterId" to id)),
        getCharacterDetail = GetCharacterDetailUseCase(repository),
        logger = logger,
    )

    @Test
    fun `loads character on start and maps it to ui model`() = runTest {
        repository.givenDetail(DomainResult.Success(morty))

        val viewModel = createViewModel()
        assertEquals(DetailState.Loading, viewModel.state.value)

        advanceUntilIdle()
        assertEquals(DetailState.Content(morty.toUi()), viewModel.state.value)
    }

    @Test
    fun `404 shows empty state and is not logged`() = runTest {
        repository.givenDetail(DomainResult.Failure(DomainError.NotFound))

        val viewModel = createViewModel(id = 9999)
        advanceUntilIdle()

        assertEquals(DetailState.Empty, viewModel.state.value)
        assertTrue(logger.warnings.isEmpty())
    }

    @Test
    fun `network error shows error, is logged, and retry loads the character`() = runTest {
        repository.givenDetail(
            DomainResult.Failure(DomainError.Network()),
            DomainResult.Success(morty),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(DetailState.Error(UiError.NETWORK), viewModel.state.value)
        assertEquals(DetailViewModel.TAG, logger.warnings.single().tag)

        viewModel.onIntent(DetailIntent.Retry)
        advanceUntilIdle()
        assertEquals(DetailState.Content(morty.toUi()), viewModel.state.value)
    }

    @Test
    fun `back click emits navigate back effect`() = runTest {
        repository.givenDetail(DomainResult.Success(morty))
        val viewModel = createViewModel()

        viewModel.onIntent(DetailIntent.BackClicked)

        assertEquals(DetailEffect.NavigateBack, viewModel.effects.first())
    }
}
