package com.example.pruebatecnica.data.di

import com.example.pruebatecnica.data.local.CharacterLocalDataSource
import com.example.pruebatecnica.data.local.InMemoryCharacterLocalDataSource
import com.example.pruebatecnica.data.remote.CharacterRemoteDataSource
import com.example.pruebatecnica.data.remote.RetrofitCharacterRemoteDataSource
import com.example.pruebatecnica.data.repository.CharacterRepositoryImpl
import com.example.pruebatecnica.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds data implementations to their abstractions. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    abstract fun bindCharacterRemoteDataSource(
        impl: RetrofitCharacterRemoteDataSource,
    ): CharacterRemoteDataSource

    @Binds
    abstract fun bindCharacterLocalDataSource(
        impl: InMemoryCharacterLocalDataSource,
    ): CharacterLocalDataSource
}
