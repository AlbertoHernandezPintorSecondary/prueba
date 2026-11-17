package com.example.pruebatecnica.data.cache

import com.example.pruebatecnica.data.FakeTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class InMemoryCacheTest {

    private val clock = FakeTimeProvider()
    private val cache = InMemoryCache<String, String>(ttl = 5.minutes, timeProvider = clock)

    // region TTL

    @Test
    fun `returns null for a missing key`() {
        assertNull(cache.get("rick"))
    }

    @Test
    fun `returns stored value while it is not expired`() {
        cache.put("rick", "value")
        clock.advanceBy(4.minutes + 59.seconds)

        assertEquals("value", cache.get("rick"))
    }

    @Test
    fun `returns null once the ttl has elapsed`() {
        cache.put("rick", "value")
        clock.advanceBy(5.minutes)

        assertNull(cache.get("rick"))
    }

    @Test
    fun `putting again refreshes the value and its timestamp`() {
        cache.put("rick", "old")
        clock.advanceBy(4.minutes)
        cache.put("rick", "new")
        clock.advanceBy(4.minutes)

        assertEquals("new", cache.get("rick"))
    }

    @Test
    fun `entries expire independently`() {
        cache.put("rick", "r")
        clock.advanceBy(3.minutes)
        cache.put("morty", "m")
        clock.advanceBy(3.minutes)

        assertNull(cache.get("rick"))
        assertEquals("m", cache.get("morty"))
    }

    @Test
    fun `expired entries are purged on write even if never read again`() {
        cache.put("rick", "r")
        cache.put("morty", "m")
        clock.advanceBy(5.minutes)

        cache.put("summer", "s")

        assertEquals(1, cache.size)
    }

    @Test
    fun `clear removes everything`() {
        cache.put("rick", "r")
        cache.clear()

        assertNull(cache.get("rick"))
    }

    // endregion

    // region size limit (LRU)

    @Test
    fun `never holds more than maxEntries`() {
        val small = InMemoryCache<Int, String>(ttl = 5.minutes, maxEntries = 3, timeProvider = clock)

        (1..10).forEach { small.put(it, "v$it") }

        assertEquals(3, small.size)
        assertEquals("v10", small.get(10))
        assertNull(small.get(1))
    }

    @Test
    fun `evicts the least recently used entry`() {
        val small = InMemoryCache<String, String>(ttl = 5.minutes, maxEntries = 2, timeProvider = clock)
        small.put("rick", "r")
        small.put("morty", "m")

        small.get("rick") // "rick" is now the most recently used
        small.put("summer", "s")

        assertEquals("r", small.get("rick"))
        assertNull("least recently used is evicted", small.get("morty"))
        assertEquals("s", small.get("summer"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `maxEntries must be positive`() {
        InMemoryCache<String, String>(ttl = 5.minutes, maxEntries = 0)
    }

    // endregion
}
