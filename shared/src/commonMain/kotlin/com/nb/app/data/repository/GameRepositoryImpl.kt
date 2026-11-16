package com.nb.app.data.repository

import com.nb.app.core.network.safeApiCall
import com.nb.app.data.mapper.toDomain
import com.nb.app.data.mapper.toPage
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page
import com.nb.app.domain.repository.GameRepository

internal class GameRepositoryImpl(
    private val remoteDataSource: NbaRemoteDataSource,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) : GameRepository {

    override suspend fun getTeamGames(teamId: Int, cursor: Int?): Result<Page<Game>> = safeApiCall {
        remoteDataSource.getGames(teamId = teamId, cursor = cursor, perPage = pageSize)
            .toPage { it.toDomain() }
    }
}
