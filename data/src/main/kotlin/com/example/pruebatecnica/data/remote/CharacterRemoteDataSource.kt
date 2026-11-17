package com.example.pruebatecnica.data.remote

import com.example.pruebatecnica.data.remote.dto.CharacterDto
import com.example.pruebatecnica.data.remote.dto.CharactersResponseDto
import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.result.DomainResult
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Remote source of characters. Returns the API response models (DTOs) as they come, wrapped
 * in a [DomainResult]; mapping them to domain models is the repository's job.
 *
 * This is the only place where transport exceptions are caught: from here up, errors are
 * typed values ([DomainError]), never exceptions.
 */
interface CharacterRemoteDataSource {
    suspend fun searchCharacters(query: String, page: Int): DomainResult<CharactersResponseDto>
    suspend fun getCharacter(id: Int): DomainResult<CharacterDto>
}

class RetrofitCharacterRemoteDataSource @Inject constructor(
    private val api: RickAndMortyApi,
) : CharacterRemoteDataSource {

    override suspend fun searchCharacters(query: String, page: Int): DomainResult<CharactersResponseDto> =
        apiCall { api.searchCharacters(query, page) }

    override suspend fun getCharacter(id: Int): DomainResult<CharacterDto> =
        apiCall { api.getCharacter(id) }

    /**
     * Runs the call and turns any failure into a typed [DomainError].
     * Cancellation is always rethrown so a cancelled search really stops.
     * HTTP 404 becomes [DomainError.NotFound]; the presentation layer shows it as an empty state.
     */
    private inline fun <T> apiCall(block: () -> T): DomainResult<T> =
        try {
            DomainResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            DomainResult.Failure(
                if (e.code() == HTTP_NOT_FOUND) DomainError.NotFound else DomainError.Server(e.code()),
            )
        } catch (e: IOException) {
            DomainResult.Failure(DomainError.Network(e))
        } catch (e: Exception) {
            // Unexpected response format (SerializationException) or any other bug.
            DomainResult.Failure(DomainError.Unknown(e))
        }
}
