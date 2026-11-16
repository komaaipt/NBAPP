package com.nb.app.testing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class FakeClock(private var now: Instant = Instant.parse("2026-11-16T12:00:00Z")) : Clock {
    override fun now(): Instant = now

    fun advanceBy(duration: Duration) {
        now += duration
    }
}
