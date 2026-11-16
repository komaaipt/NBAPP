package com.nb.app.data.cache

import com.nb.app.testing.FakeClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryCacheTest {

    private val clock = FakeClock()
    private var loads = 0
    private val load: suspend () -> String = { "value${++loads}" }

    @Test
    fun cachedValue_isReusedUntilItExpires() = runTest {
        val cache = MemoryCache<String, String>(clock)

        assertEquals("value1", cache.getOrLoad("key", 5.minutes, load))
        clock.advanceBy(4.minutes)
        assertEquals("value1", cache.getOrLoad("key", 5.minutes, load))
        clock.advanceBy(2.minutes)
        assertEquals("value2", cache.getOrLoad("key", 5.minutes, load))
    }

    @Test
    fun valueWithoutTimeToLive_neverExpires() = runTest {
        val cache = MemoryCache<String, String>(clock)

        cache.getOrLoad("key", timeToLive = null, load)
        clock.advanceBy(1_000.minutes)

        assertEquals("value1", cache.getOrLoad("key", timeToLive = null, load))
    }

    @Test
    fun failedLoad_isNotCached() = runTest {
        val cache = MemoryCache<String, String>(clock)

        assertFailsWith<IllegalStateException> { cache.getOrLoad("key", 5.minutes) { error("offline") } }

        assertEquals("value1", cache.getOrLoad("key", 5.minutes, load))
    }

    @Test
    fun leastRecentlyUsedEntry_isDroppedWhenFull() = runTest {
        val cache = MemoryCache<String, String>(clock, maxEntries = 2)
        cache.getOrLoad("a", null, load)
        cache.getOrLoad("b", null, load)
        cache.getOrLoad("a", null, load)

        cache.getOrLoad("c", null, load)

        assertEquals("value1", cache.getOrLoad("a", null, load))
        assertEquals("value4", cache.getOrLoad("b", null, load))
    }

    @Test
    fun cachedKey_answersWhileAnotherKeyIsLoading() = runTest {
        val cache = MemoryCache<String, String>(clock)
        cache.getOrLoad("cached", null) { "hit" }
        val gate = CompletableDeferred<Unit>()
        val slowLoad = launch { cache.getOrLoad("slow", null) { gate.await(); "slow" } }
        runCurrent()

        val hit = withTimeout(1.seconds) { cache.getOrLoad("cached", null) { error("Not cached") } }

        assertEquals("hit", hit)
        gate.complete(Unit)
        slowLoad.join()
    }

    @Test
    fun differentKeys_loadInParallel() = runTest {
        val cache = MemoryCache<String, String>(clock)
        val gate = CompletableDeferred<Unit>()
        var started = 0

        val a = async { cache.getOrLoad("a", null) { started++; gate.await(); "a" } }
        val b = async { cache.getOrLoad("b", null) { started++; gate.await(); "b" } }
        runCurrent()

        assertEquals(2, started)
        gate.complete(Unit)
        assertEquals("a", a.await())
        assertEquals("b", b.await())
    }

    @Test
    fun sameKeyAtTheSameTime_loadsOnce() = runTest {
        val cache = MemoryCache<String, String>(clock)
        val gate = CompletableDeferred<Unit>()

        val first = async { cache.getOrLoad("key", null) { loads++; gate.await(); "shared" } }
        val second = async { cache.getOrLoad("key", null) { loads++; "duplicate" } }
        runCurrent()
        gate.complete(Unit)

        assertEquals("shared", first.await())
        assertEquals("shared", second.await())
        assertEquals(1, loads)
    }

    @Test
    fun cancelledLoad_isTakenOverByTheNextCaller() = runTest {
        val cache = MemoryCache<String, String>(clock)
        val gate = CompletableDeferred<Unit>()
        val first = launch { cache.getOrLoad("key", null) { gate.await(); "first" } }
        runCurrent()
        val second = async { cache.getOrLoad("key", null) { "second" } }
        runCurrent()

        first.cancel()

        assertEquals("second", second.await())
        assertEquals("second", cache.getOrLoad("key", null) { error("Not cached") })
    }
}
