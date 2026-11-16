package com.nb.app.presentation.teamgames

import com.nb.app.domain.model.Game

private val MonthAbbreviations = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/** "2026-11-15" → "Nov 15, 2026". Returns the input unchanged if it isn't an ISO date. */
internal fun formatGameDate(isoDate: String): String {
    val parts = isoDate.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull()
    val month = parts.getOrNull(1)?.toIntOrNull()
    val day = parts.getOrNull(2)?.take(2)?.toIntOrNull()
    if (year == null || month == null || day == null || month !in 1..12) return isoDate
    return "${MonthAbbreviations[month - 1]} $day, $year"
}

/** 2026 → "2026–27". */
internal fun formatSeason(season: Int): String =
    "$season–${((season + 1) % 100).toString().padStart(2, '0')}"

/** Whether the team with [teamId] won [game]; `null` for ties or games it didn't play. */
internal fun Game.isWinFor(teamId: Int): Boolean? {
    val ownScore = when (teamId) {
        homeTeam.id -> homeTeamScore
        visitorTeam.id -> visitorTeamScore
        else -> return null
    }
    val otherScore = homeTeamScore + visitorTeamScore - ownScore
    return if (ownScore == otherScore) null else ownScore > otherScore
}
