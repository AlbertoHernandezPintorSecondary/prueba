package com.example.pruebatecnica.data.remote

import com.example.pruebatecnica.data.remote.dto.CharacterDto
import com.example.pruebatecnica.data.remote.dto.CharactersResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * https://rickandmortyapi.com/documentation/#rest
 *
 * Retrofit `suspend` functions are cancellable: cancelling the calling coroutine
 * cancels the underlying OkHttp call.
 */
interface RickAndMortyApi {

    /** Returns 20 results per page. Answers 404 when there are no matches. */
    @GET("character/")
    suspend fun searchCharacters(
        @Query("name") name: String,
        @Query("page") page: Int,
    ): CharactersResponseDto

    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): CharacterDto

    companion object {
        const val BASE_URL = "https://rickandmortyapi.com/api/"
    }
}
