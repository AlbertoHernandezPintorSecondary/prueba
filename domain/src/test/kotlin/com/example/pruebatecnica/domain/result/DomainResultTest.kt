package com.example.pruebatecnica.domain.result

import com.example.pruebatecnica.domain.error.DomainError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainResultTest {

    @Test
    fun `map transforms success data`() {
        val result: DomainResult<Int> = DomainResult.Success(2)

        assertEquals(DomainResult.Success("2"), result.map { it.toString() })
    }

    @Test
    fun `map keeps failures untouched`() {
        val failure: DomainResult<Int> = DomainResult.Failure(DomainError.NotFound)

        assertEquals(failure, failure.map { it.toString() })
    }

    @Test
    fun `onSuccess only runs for success`() {
        var called = false
        DomainResult.Success(1).onSuccess { called = true }
        assertTrue(called)

        called = false
        DomainResult.Failure(DomainError.Network()).onSuccess { called = true }
        assertFalse(called)
    }
}
