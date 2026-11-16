package com.nb.app.presentation.splash

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/*
 * Pure description of the splash animation: given the elapsed time it returns the state of every
 * element. Drawing lives in SplashScreen.kt, so this file can be unit tested.
 *
 * Timeline: the ball is thrown in an arc from the bottom-left corner, lands in the middle of the
 * screen with a small bounce, and then the app name fades in below it.
 */

internal const val FLIGHT_END_MILLIS = 750f
internal const val BOUNCE_END_MILLIS = FLIGHT_END_MILLIS + 380f
internal const val NAME_START_MILLIS = FLIGHT_END_MILLIS + 150f
internal const val NAME_END_MILLIS = NAME_START_MILLIS + 550f
internal const val SPLASH_DURATION_MILLIS = 2_000f

/** Height of the bounce after landing, in ball radiuses. */
internal const val BOUNCE_HEIGHT = 0.35f

internal data class SplashFrame(
    val flight: Float,
    val arc: Float,
    val bounceLift: Float,
    val ballScale: Float,
    val ballRotationDegrees: Float,
    val ballSquash: Float,
    val shadowAlpha: Float,
    val nameAlpha: Float,
)

internal fun splashFrame(elapsedMillis: Float): SplashFrame {
    val t = elapsedMillis.coerceIn(0f, SPLASH_DURATION_MILLIS)
    val flight = (t / FLIGHT_END_MILLIS).coerceIn(0f, 1f)
    val hasLanded = t >= FLIGHT_END_MILLIS
    val bounce = ((t - FLIGHT_END_MILLIS) / (BOUNCE_END_MILLIS - FLIGHT_END_MILLIS)).coerceIn(0f, 1f)

    return SplashFrame(
        flight = flight,
        // A thrown ball: a parabola on top of the straight path, highest halfway through.
        arc = 4f * flight * (1f - flight),
        bounceLift = if (hasLanded) BOUNCE_HEIGHT * sin(bounce * PI.toFloat()) else 0f,
        // It comes from further away, so it grows while flying towards the screen.
        ballScale = 0.55f + 0.45f * easeOut(flight),
        ballRotationDegrees = 600f * easeOut(flight) + 40f * easeOut(bounce),
        // Squashes right when it lands and recovers in the first fifth of the bounce.
        ballSquash = if (hasLanded) (1f - bounce / 0.2f).coerceIn(0f, 1f) else 0f,
        shadowAlpha = 0.35f * easeOut(flight),
        nameAlpha = easeOut((t - NAME_START_MILLIS) / (NAME_END_MILLIS - NAME_START_MILLIS)),
    )
}

internal fun lerp(from: Float, to: Float, fraction: Float) = from + (to - from) * fraction

internal fun easeOut(x: Float) = 1f - (1f - x.coerceIn(0f, 1f)).pow(3)
