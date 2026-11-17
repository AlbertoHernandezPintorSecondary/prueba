package com.example.pruebatecnica.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharactersResponseDto(
    val info: PageInfoDto,
    val results: List<CharacterDto>,
)

@Serializable
data class PageInfoDto(
    val count: Int,
    val pages: Int,
    val next: String? = null,
    val prev: String? = null,
)

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String = "",
    val gender: String,
    val origin: NamedLinkDto,
    val location: NamedLinkDto,
    val image: String,
    val episode: List<String> = emptyList(),
)

@Serializable
data class NamedLinkDto(
    val name: String,
    val url: String = "",
)
