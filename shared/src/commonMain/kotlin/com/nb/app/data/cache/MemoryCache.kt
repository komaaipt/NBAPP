package com.nb.app.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * In-memory cache for the app session. Failed loads are never stored, and the oldest entry is
 * dropped once [maxEntries] is exceeded.
 */
internal class MemoryCache<K : Any, V : Any>(
    private val clock: Clock = Clock.System,
    private val maxEntries: Int = 100,
) {
    private class Entry<V>(val value: V, val expiresAt: Instant?)

    private val mutex = Mutex()
    private val entries = LinkedHashMap<K, Entry<V>>()

    /** Returns the cached value for [key] or loads and stores it; `null` [timeToLive] never expires. */
    suspend fun getOrLoad(key: K, timeToLive: Duration?, load: suspend () -> V): V = mutex.withLock {
        val cached = entries[key]
        if (cached != null && (cached.expiresAt == null || clock.now() < cached.expiresAt)) {
            return@withLock cached.value
        }
        val value = load()
        entries.remove(key)
        entries[key] = Entry(value, timeToLive?.let { clock.now() + it })
        if (entries.size > maxEntries) entries.remove(entries.keys.first())
        value
    }
}
