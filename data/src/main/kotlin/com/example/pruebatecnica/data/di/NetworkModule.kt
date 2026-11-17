package com.example.pruebatecnica.data.di

import com.example.pruebatecnica.data.remote.RetrofitFactory
import com.example.pruebatecnica.data.remote.RickAndMortyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = RetrofitFactory.json

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRickAndMortyApi(client: OkHttpClient, json: Json): RickAndMortyApi =
        RetrofitFactory.createApi(RickAndMortyApi.BASE_URL, client, json)
}
