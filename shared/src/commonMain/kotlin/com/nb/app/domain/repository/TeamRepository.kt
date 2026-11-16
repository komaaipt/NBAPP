package com.nb.app.domain.repository

import com.nb.app.domain.model.Team

interface TeamRepository {
    /**
     * Returns the current NBA teams (defunct franchises are left out). The result is cached in memory; pass [forceRefresh] to hit the API again.
     */
    suspend fun getTeams(forceRefresh: Boolean = false): Result<List<Team>>
}
