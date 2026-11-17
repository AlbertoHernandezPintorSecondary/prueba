package com.example.pruebatecnica.data.cache

/**
 * Abstraction over the clock so cache expiration can be tested deterministically.
 */
fun interface TimeProvider {
    fun nowMillis(): Long

    companion object {
        /** Monotonic clock: not affected by the user changing the device time. */
        val Monotonic = TimeProvider { System.nanoTime() / 1_000_000 }
    }
}
