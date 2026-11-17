package com.example.pruebatecnica.data.cache

import kotlin.time.Duration

/**
 * Thread-safe in-memory cache with:
 *  - **TTL**: entries expire after [ttl]. Expired entries are evicted when read and
 *    purged on every write, so stale data does not pile up.
 *  - **Size limit (LRU)**: at most [maxEntries] entries; when full, the least recently
 *    used entry is evicted. Memory stays bounded however many searches the user makes.
 */
class InMemoryCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val timeProvider: TimeProvider = TimeProvider.Monotonic,
) {
    init {
        require(maxEntries > 0) { "maxEntries must be > 0" }
    }

    private data class Entry<V>(val value: V, val storedAtMillis: Long)

    private val lock = Any()

    /** accessOrder = true: iteration goes from least to most recently used (LRU order). */
    private val entries = LinkedHashMap<K, Entry<V>>(16, 0.75f, true)

    val size: Int
        get() = synchronized(lock) { entries.size }

    /** Returns the cached value if present and not expired, otherwise `null`. */
    fun get(key: K): V? = synchronized(lock) {
        val entry = entries[key] ?: return null
        if (entry.isExpired(timeProvider.nowMillis())) {
            entries.remove(key)
            null
        } else {
            entry.value
        }
    }

    fun put(key: K, value: V) = synchronized(lock) {
        val now = timeProvider.nowMillis()
        entries[key] = Entry(value, now)
        entries.values.removeAll { it.isExpired(now) }
        while (entries.size > maxEntries) {
            entries.remove(entries.keys.first())
        }
    }

    fun clear() = synchronized(lock) { entries.clear() }

    private fun Entry<V>.isExpired(now: Long): Boolean =
        now - storedAtMillis >= ttl.inWholeMilliseconds

    companion object {
        const val DEFAULT_MAX_ENTRIES = 100
    }
}
