package com.example.pruebatecnica.domain.error

/**
 * Errors the domain understands, as plain values (not exceptions).
 *
 * The data layer translates transport-specific exceptions (IOException, HttpException...)
 * into one of these, so upper layers never depend on networking libraries. Being a sealed
 * type, every `when` over it must handle all cases: the compiler enforces error handling.
 *
 * [cause] keeps the original exception only for diagnostics (logging).
 */
sealed interface DomainError {
    val cause: Throwable? get() = null

    /** No connection, timeout, DNS or SSL failure... */
    data class Network(override val cause: Throwable? = null) : DomainError

    /** The resource does not exist (HTTP 404). Searches with no matches also answer 404. */
    data object NotFound : DomainError

    /** The server answered with an error status (other than 404). */
    data class Server(val code: Int) : DomainError

    /** Anything else (unexpected response format, bugs...). */
    data class Unknown(override val cause: Throwable? = null) : DomainError
}
