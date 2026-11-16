package com.nb.app.data.cache

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * In-memory cache for the app session with least-recently-used eviction. Concurrent requests for
 * the same key share one load, loads for different keys run in parallel, and failures are never
 * stored.
 */
internal class MemoryCache<K : Any, V : Any>(
    private val clock: Clock = Clock.System,
    private val maxEntries: Int = 100,
) {
    private class Entry<V>(val value: V, val expiresAt: Instant?)

    private val mutex = Mutex()
    private val entries = LinkedHashMap<K, Entry<V>>()
    private val pendingLoads = HashMap<K, CompletableDeferred<V>>()

    /** Returns the cached value for [key] or loads it; a `null` [timeToLive] never expires. */
    suspend fun getOrLoad(key: K, timeToLive: Duration?, load: suspend () -> V): V {
        while (true) {
            val (pending, isOwner) = mutex.withLock {
                freshValue(key)?.let { return it }
                val existing = pendingLoads[key]
                if (existing != null) {
                    existing to false
                } else {
                    CompletableDeferred<V>().also { pendingLoads[key] = it } to true
                }
            }
            if (isOwner) return loadAndStore(key, timeToLive, pending, load)
            try {
                return pending.await()
            } catch (e: CancellationException) {
                // The owner was cancelled: take over the load unless this caller was cancelled too.
                currentCoroutineContext().ensureActive()
            }
        }
    }

    private fun freshValue(key: K): V? {
        val entry = entries.remove(key) ?: return null
        if (entry.expiresAt != null && clock.now() >= entry.expiresAt) return null
        entries[key] = entry
        return entry.value
    }

    private suspend fun loadAndStore(
        key: K,
        timeToLive: Duration?,
        pending: CompletableDeferred<V>,
        load: suspend () -> V,
    ): V {
        val value = try {
            load()
        } catch (e: Throwable) {
            withContext(NonCancellable) {
                mutex.withLock { pendingLoads.remove(key) }
                pending.completeExceptionally(e)
            }
            throw e
        }
        withContext(NonCancellable) {
            mutex.withLock {
                entries[key] = Entry(value, timeToLive?.let { clock.now() + it })
                if (entries.size > maxEntries) entries.remove(entries.keys.first())
                pendingLoads.remove(key)
            }
            pending.complete(value)
        }
        return value
    }
}
