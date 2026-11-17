package com.example.pruebatecnica.domain.result

import com.example.pruebatecnica.domain.error.DomainError

/**
 * Typed result of a domain operation: either the data or a [DomainError].
 *
 * Unlike `kotlin.Result` (whose failure is any `Throwable`), the error type is known and
 * sealed, so callers handle every case with an exhaustive `when`. Errors are values, so
 * nothing needs to catch exceptions and coroutine cancellation is never mistaken for a failure.
 */
sealed interface DomainResult<out T> {
    data class Success<out T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

/** Transforms the data of a [DomainResult.Success]; a failure is returned as is. */
inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(data))
    is DomainResult.Failure -> this
}

/** Runs [action] with the data when this is a [DomainResult.Success]. */
inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> {
    if (this is DomainResult.Success) action(data)
    return this
}
