package com.nb.app.domain.repository

import com.nb.app.domain.model.Team

interface TeamRepository {
    /**
     * Returns every team. The result is cached in memory; pass [forceRefresh] to hit the API again.
     */
    suspend fun getTeams(forceRefresh: Boolean = false): Result<List<Team>>
}
