package com.nb.app.data.repository

import com.nb.app.core.network.safeApiCall
import com.nb.app.data.mapper.toDomain
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.domain.model.Team
import com.nb.app.domain.repository.TeamRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class TeamRepositoryImpl(
    private val remoteDataSource: NbaRemoteDataSource,
) : TeamRepository {

    // The free API tier allows only a few requests per minute and the team list rarely
    // changes, so it is kept in memory for the lifetime of the app.
    private val mutex = Mutex()
    private var cachedTeams: List<Team>? = null

    override suspend fun getTeams(forceRefresh: Boolean): Result<List<Team>> = mutex.withLock {
        val cached = cachedTeams
        if (cached != null && !forceRefresh) {
            return@withLock Result.success(cached)
        }
        safeApiCall {
            remoteDataSource.getTeams().data
                // The API also returns defunct franchises, which have no conference.
                .filter { it.conference.isNotBlank() }
                .map { it.toDomain() }
        }
            .onSuccess { cachedTeams = it }
    }
}
