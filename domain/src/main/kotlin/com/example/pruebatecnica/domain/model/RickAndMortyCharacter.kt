package com.example.pruebatecnica.domain.model

/**
 * Domain representation of a Rick and Morty character.
 * Named explicitly to avoid any confusion with [kotlin.Char] / `java.lang.Character`.
 * Framework-agnostic: knows nothing about JSON, Retrofit or Android.
 */
data class RickAndMortyCharacter(
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val type: String,
    val gender: String,
    val originName: String,
    val locationName: String,
    val imageUrl: String,
    val episodeCount: Int,
)

enum class CharacterStatus { ALIVE, DEAD, UNKNOWN }
