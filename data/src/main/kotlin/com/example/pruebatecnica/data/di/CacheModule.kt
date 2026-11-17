package com.example.pruebatecnica.data.di

import com.example.pruebatecnica.data.cache.InMemoryCache
import com.example.pruebatecnica.data.cache.SearchCacheKey
import com.example.pruebatecnica.data.cache.TimeProvider
import com.example.pruebatecnica.domain.model.CharacterPage
import com.example.pruebatecnica.domain.model.RickAndMortyCharacter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    /** Cached results are valid for 5 minutes. */
    private val CACHE_TTL: Duration = 5.minutes

    /** Up to 50 search pages (≈1000 characters) are kept; least recently used are evicted first. */
    private const val MAX_SEARCH_PAGES = 50

    /** Characters indexed by id for the detail screen. */
    private const val MAX_CHARACTERS = 1_000

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = TimeProvider.Monotonic

    @Provides
    @Singleton
    fun provideSearchCache(timeProvider: TimeProvider): InMemoryCache<SearchCacheKey, CharacterPage> =
        InMemoryCache(ttl = CACHE_TTL, maxEntries = MAX_SEARCH_PAGES, timeProvider = timeProvider)

    @Provides
    @Singleton
    fun provideCharacterCache(timeProvider: TimeProvider): InMemoryCache<Int, RickAndMortyCharacter> =
        InMemoryCache(ttl = CACHE_TTL, maxEntries = MAX_CHARACTERS, timeProvider = timeProvider)
}
