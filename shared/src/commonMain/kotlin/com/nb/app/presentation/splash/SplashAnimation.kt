package com.nb.app.presentation.splash

import kotlin.math.pow

internal const val BADGE_END_MILLIS = 450f
internal const val WORDMARK_START_MILLIS = 250f
internal const val WORDMARK_END_MILLIS = 750f
internal const val SPLASH_DURATION_MILLIS = 1_600f

internal data class SplashFrame(
    val badgeAlpha: Float,
    val badgeScale: Float,
    val wordmarkAlpha: Float,
    val wordmarkSlide: Float,
)

internal fun splashFrame(elapsedMillis: Float): SplashFrame {
    val t = elapsedMillis.coerceIn(0f, SPLASH_DURATION_MILLIS)
    val badge = easeOut(t / BADGE_END_MILLIS)
    val wordmark = easeOut((t - WORDMARK_START_MILLIS) / (WORDMARK_END_MILLIS - WORDMARK_START_MILLIS))
    return SplashFrame(
        badgeAlpha = badge,
        badgeScale = 0.85f + 0.15f * badge,
        wordmarkAlpha = wordmark,
        wordmarkSlide = 1f - wordmark,
    )
}

internal fun easeOut(x: Float) = 1f - (1f - x.coerceIn(0f, 1f)).pow(3)
