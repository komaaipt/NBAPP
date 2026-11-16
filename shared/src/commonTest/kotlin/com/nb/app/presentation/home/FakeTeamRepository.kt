package com.nb.app.presentation.home

import com.nb.app.domain.model.Team
import com.nb.app.domain.repository.TeamRepository
import kotlinx.coroutines.CompletableDeferred

class FakeTeamRepository(
    var result: Result<List<Team>> = Result.success(emptyList()),
) : TeamRepository {

    var callCount = 0
        private set
    var lastForceRefresh: Boolean? = null
        private set

    /** When set, [getTeams] suspends until it is completed. */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getTeams(forceRefresh: Boolean): Result<List<Team>> {
        callCount++
        lastForceRefresh = forceRefresh
        gate?.await()
        return result
    }
}
