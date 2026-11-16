package com.nb.app.presentation.teamgames

import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page
import com.nb.app.domain.repository.GameRepository
import kotlinx.coroutines.CompletableDeferred

/** Answers each season with the result set in [results]; `null` stands for the current season. */
class FakeGameRepository : GameRepository {

    val results = mutableMapOf<Int?, Result<Page<Game>>>()
    val requestedSeasons = mutableListOf<Int?>()

    /** When set, [getTeamGames] suspends until it is completed. */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getTeamGames(teamId: Int, season: Int?): Result<Page<Game>> {
        requestedSeasons += season
        gate?.await()
        return results[season] ?: error("No result for season $season")
    }
}
