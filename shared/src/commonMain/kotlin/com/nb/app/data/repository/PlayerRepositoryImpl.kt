package com.nb.app.data.repository

import com.nb.app.core.network.safeApiCall
import com.nb.app.data.cache.MemoryCache
import com.nb.app.data.mapper.toDomain
import com.nb.app.data.mapper.toPage
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.domain.model.Page
import com.nb.app.domain.model.Player
import com.nb.app.domain.repository.PlayerRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

private val SearchTimeToLive = 5.minutes

internal class PlayerRepositoryImpl(
    private val remoteDataSource: NbaRemoteDataSource,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    clock: Clock = Clock.System,
) : PlayerRepository {

    private val searchCache = MemoryCache<Pair<String, Int?>, Page<Player>>(clock)

    override suspend fun searchPlayers(query: String, cursor: Int?): Result<Page<Player>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return Result.success(Page(items = emptyList(), nextCursor = null))
        }
        return safeApiCall {
            searchCache.getOrLoad(key = trimmedQuery.lowercase() to cursor, timeToLive = SearchTimeToLive) {
                remoteDataSource.searchPlayers(query = trimmedQuery, cursor = cursor, perPage = pageSize)
                    .toPage { it.toDomain() }
            }
        }
    }
}
