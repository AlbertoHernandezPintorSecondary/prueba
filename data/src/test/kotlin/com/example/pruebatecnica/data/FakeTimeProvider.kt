package com.example.pruebatecnica.data

import com.example.pruebatecnica.data.cache.TimeProvider
import kotlin.time.Duration

/** Manually controlled clock for deterministic cache-expiration tests. */
class FakeTimeProvider(var currentMillis: Long = 0L) : TimeProvider {
    override fun nowMillis(): Long = currentMillis

    fun advanceBy(duration: Duration) {
        currentMillis += duration.inWholeMilliseconds
    }
}
