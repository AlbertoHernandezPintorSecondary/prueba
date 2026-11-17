package com.example.pruebatecnica.data.cache

/** Search results are cached per (normalized query, page). */
data class SearchCacheKey(val query: String, val page: Int)
