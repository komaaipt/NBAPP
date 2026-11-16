package com.nb.app.presentation.splash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplashAnimationTest {

    @Test
    fun logo_startsHidden() {
        val start = splashFrame(0f)

        assertEquals(0f, start.badgeAlpha)
        assertEquals(0f, start.wordmarkAlpha)
        assertEquals(1f, start.wordmarkSlide)
    }

    @Test
    fun badge_entersBeforeTheWordmark() {
        val frame = splashFrame(WORDMARK_START_MILLIS)

        assertTrue(frame.badgeAlpha > 0.5f)
        assertEquals(0f, frame.wordmarkAlpha)
    }

    @Test
    fun badge_growsToItsFullSize() {
        assertTrue(splashFrame(0f).badgeScale < 1f)
        assertEquals(1f, splashFrame(BADGE_END_MILLIS).badgeScale)
    }

    @Test
    fun logo_isFullyShownBeforeTheSplashEnds() {
        val end = splashFrame(WORDMARK_END_MILLIS)

        assertEquals(1f, end.badgeAlpha)
        assertEquals(1f, end.wordmarkAlpha)
        assertEquals(0f, end.wordmarkSlide)
        assertTrue(WORDMARK_END_MILLIS < SPLASH_DURATION_MILLIS)
    }
}
