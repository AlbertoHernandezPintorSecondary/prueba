package com.example.pruebatecnica.presentation.common.model

import androidx.annotation.StringRes
import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.presentation.R

/** Presentation-level error: what the UI needs to pick a message, nothing more. */
enum class UiError { NETWORK, NOT_FOUND, SERVER, UNKNOWN }

/**
 * Exhaustive mapping: adding a new [DomainError] breaks the build here until it is handled.
 * (The screens show [DomainError.NotFound] as an empty state before ever reaching this.)
 */
fun DomainError.toUiError(): UiError = when (this) {
    is DomainError.Network -> UiError.NETWORK
    DomainError.NotFound -> UiError.NOT_FOUND
    is DomainError.Server -> UiError.SERVER
    is DomainError.Unknown -> UiError.UNKNOWN
}

@StringRes
fun UiError.messageRes(): Int = when (this) {
    UiError.NETWORK -> R.string.error_network
    UiError.NOT_FOUND -> R.string.error_not_found
    UiError.SERVER -> R.string.error_server
    UiError.UNKNOWN -> R.string.error_unknown
}
