package com.nb.app.presentation.players

import com.nb.app.domain.model.Page
import com.nb.app.domain.model.Player
import com.nb.app.domain.repository.PlayerRepository
import kotlinx.coroutines.CompletableDeferred

/** Answers each (query, cursor) pair with the result set in [results]. */
class FakePlayerRepository : PlayerRepository {

    val results = mutableMapOf<Pair<String, Int?>, Result<Page<Player>>>()
    val requests = mutableListOf<Pair<String, Int?>>()

    /** When set, [searchPlayers] suspends until it is completed. */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun searchPlayers(query: String, cursor: Int?): Result<Page<Player>> {
        val request = query to cursor
        requests += request
        gate?.await()
        return results[request] ?: error("No result for $request")
    }
}
