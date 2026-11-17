package com.example.pruebatecnica.domain.fixtures

import com.example.pruebatecnica.domain.error.DomainError
import com.example.pruebatecnica.domain.result.DomainResult

/** Returns the data of a success, or fails the test with a readable message. */
fun <T> DomainResult<T>.successData(): T = when (this) {
    is DomainResult.Success -> data
    is DomainResult.Failure -> throw AssertionError("Expected Success but was Failure($error)")
}

/** Returns the error of a failure, or fails the test with a readable message. */
fun DomainResult<*>.failureError(): DomainError = when (this) {
    is DomainResult.Failure -> error
    is DomainResult.Success -> throw AssertionError("Expected Failure but was Success($data)")
}
