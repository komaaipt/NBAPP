package com.nb.app.data.repository

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class NbaSeasonTest {

    private fun seasonOn(isoInstant: String): Int {
        val clock = object : Clock {
            override fun now(): Instant = Instant.parse(isoInstant)
        }
        return currentNbaSeason(clock = clock, timeZone = TimeZone.UTC)
    }

    @Test
    fun fromOctober_seasonIsTheCurrentYear() {
        assertEquals(2026, seasonOn("2026-10-01T00:00:00Z"))
        assertEquals(2026, seasonOn("2026-12-31T23:59:59Z"))
    }

    @Test
    fun beforeOctober_seasonIsThePreviousYear() {
        assertEquals(2025, seasonOn("2026-01-01T00:00:00Z"))
        assertEquals(2025, seasonOn("2026-09-30T23:59:59Z"))
    }
}
