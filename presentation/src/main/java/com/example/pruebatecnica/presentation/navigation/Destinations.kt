package com.example.pruebatecnica.presentation.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. */
@Serializable
data object SearchDestination

@Serializable
data class DetailDestination(val characterId: Int)
