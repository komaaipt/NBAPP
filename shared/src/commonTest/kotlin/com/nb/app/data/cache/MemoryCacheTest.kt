package com.nb.app.data.cache

import com.nb.app.testing.FakeClock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes

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
    fun oldestEntry_isDroppedWhenFull() = runTest {
        val cache = MemoryCache<String, String>(clock, maxEntries = 2)
        cache.getOrLoad("a", null, load)
        cache.getOrLoad("b", null, load)
        cache.getOrLoad("c", null, load)

        assertEquals("value2", cache.getOrLoad("b", null, load))
        assertEquals("value4", cache.getOrLoad("a", null, load))
    }
}
