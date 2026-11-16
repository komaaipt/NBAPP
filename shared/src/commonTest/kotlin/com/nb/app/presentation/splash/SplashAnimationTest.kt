package com.nb.app.presentation.splash

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplashAnimationTest {

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 1e-3f) =
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")

    @Test
    fun throw_startsAtTheCornerAndPeaksHalfway() {
        val start = splashFrame(0f)
        assertEquals(0f, start.flight)
        assertEquals(0f, start.arc)

        val halfway = splashFrame(FLIGHT_END_MILLIS / 2f)
        assertClose(0.5f, halfway.flight)
        assertClose(1f, halfway.arc)
    }

    @Test
    fun ball_growsWhileFlyingTowardsTheScreen() {
        assertTrue(splashFrame(0f).ballScale < splashFrame(FLIGHT_END_MILLIS / 2f).ballScale)
        assertClose(1f, splashFrame(FLIGHT_END_MILLIS).ballScale)
    }

    @Test
    fun landing_squashesThenBouncesAndRests() {
        val landing = splashFrame(FLIGHT_END_MILLIS)
        assertClose(1f, landing.ballSquash)
        assertClose(0f, landing.arc)
        assertClose(0f, landing.bounceLift)

        val bounceTop = splashFrame((FLIGHT_END_MILLIS + BOUNCE_END_MILLIS) / 2f)
        assertClose(BOUNCE_HEIGHT, bounceTop.bounceLift)
        assertEquals(0f, bounceTop.ballSquash)

        val resting = splashFrame(BOUNCE_END_MILLIS)
        assertClose(0f, resting.bounceLift)
    }

    @Test
    fun name_fadesInOnlyAfterTheBallLands() {
        assertEquals(0f, splashFrame(FLIGHT_END_MILLIS).nameAlpha)
        val fading = splashFrame((NAME_START_MILLIS + NAME_END_MILLIS) / 2f).nameAlpha
        assertTrue(fading > 0f && fading < 1f)
        assertEquals(1f, splashFrame(NAME_END_MILLIS).nameAlpha)
        assertEquals(1f, splashFrame(SPLASH_DURATION_MILLIS).nameAlpha)
    }

    @Test
    fun timeline_fitsInTheSplashDuration() {
        assertTrue(BOUNCE_END_MILLIS < SPLASH_DURATION_MILLIS)
        assertTrue(NAME_END_MILLIS < SPLASH_DURATION_MILLIS)
    }
}
