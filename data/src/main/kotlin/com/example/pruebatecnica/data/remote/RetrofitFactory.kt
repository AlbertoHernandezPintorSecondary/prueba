package com.example.pruebatecnica.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Builds the Retrofit API. Shared by the Hilt module and by tests (MockWebServer). */
object RetrofitFactory {

    val json: Json = Json { ignoreUnknownKeys = true }

    fun createApi(
        baseUrl: String,
        client: OkHttpClient = OkHttpClient(),
        json: Json = this.json,
    ): RickAndMortyApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RickAndMortyApi::class.java)
}
