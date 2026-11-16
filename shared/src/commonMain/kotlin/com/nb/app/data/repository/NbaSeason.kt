package com.nb.app.data.repository

import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** First season in the balldontlie data (1946-47). */
internal const val FIRST_NBA_SEASON = 1946

/**
 * The NBA season that is current on [clock]'s date. Seasons tip off in October and are named
 * after the year they start, so November 2026 belongs to season 2026 and March 2026 to 2025.
 */
internal fun currentNbaSeason(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Int {
    val today = clock.todayIn(timeZone)
    return if (today.month >= Month.OCTOBER) today.year else today.year - 1
}
