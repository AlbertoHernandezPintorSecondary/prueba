package com.example.pruebatecnica.domain.model

/**
 * Single source of truth for what "the same search" means.
 * The API search is case-insensitive and ignores surrounding spaces, so
 * "Rick", " rick " and "RICK" are the same search.
 *
 * Used by the use case (what is sent), the repository (cache key) and the
 * search screen (to avoid re-searching when the normalized text did not change).
 */
fun normalizeSearchQuery(query: String): String = query.trim().lowercase()
