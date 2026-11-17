package com.example.pruebatecnica.presentation.common.model

import androidx.compose.runtime.Immutable

/**
 * What the UI needs to draw a character, already prepared for display.
 * Composables never see the domain model: the ViewModels map it when the use case answers.
 *
 * @property type `null` when the character has no type, so the row can simply be hidden.
 */
@Immutable
data class CharacterUi(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val status: CharacterStatusUi,
    val species: String,
    val type: String?,
    val gender: String,
    val origin: String,
    val location: String,
    val episodeCount: Int,
)
