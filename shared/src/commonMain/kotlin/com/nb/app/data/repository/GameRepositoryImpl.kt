package com.nb.app.data.repository

import com.nb.app.core.network.safeApiCall
import com.nb.app.data.cache.MemoryCache
import com.nb.app.data.mapper.toDomain
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.data.remote.dto.GameDto
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page
import com.nb.app.domain.repository.GameRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/** A regular season plus playoffs fits in one or two requests of this size (the API maximum). */
private const val SEASON_PAGE_SIZE = 100
private const val SCHEDULED_STATE = "scheduled"
private val CurrentSeasonTimeToLive = 5.minutes

internal class GameRepositoryImpl(
    private val remoteDataSource: NbaRemoteDataSource,
    private val currentSeason: () -> Int = { currentNbaSeason() },
    clock: Clock = Clock.System,
) : GameRepository {

    private val seasonCache = MemoryCache<Pair<Int, Int>, List<Game>>(clock)

    override suspend fun getTeamGames(teamId: Int, season: Int?): Result<Page<Game>> = safeApiCall {
        var loadedSeason = season ?: currentSeason()
        var games = fetchSeason(teamId, loadedSeason)

        // Right before tip-off the current season has no games played yet: show the last one.
        if (season == null && games.isEmpty() && loadedSeason > FIRST_NBA_SEASON) {
            loadedSeason -= 1
            games = fetchSeason(teamId, loadedSeason)
        }

        val previousSeason = loadedSeason - 1
        Page(
            items = games,
            // An empty season means the franchise didn't exist yet, so there is nothing older.
            nextCursor = previousSeason.takeIf { games.isNotEmpty() && it >= FIRST_NBA_SEASON },
        )
    }

    // Past seasons never change; the current one gets new games, so it expires.
    private suspend fun fetchSeason(teamId: Int, season: Int): List<Game> =
        seasonCache.getOrLoad(
            key = teamId to season,
            timeToLive = if (season >= currentSeason()) CurrentSeasonTimeToLive else null,
        ) { fetchSeasonFromApi(teamId, season) }

    /** Loads every played game of [season], following the API cursor, newest first. */
    private suspend fun fetchSeasonFromApi(teamId: Int, season: Int): List<Game> {
        val games = mutableListOf<GameDto>()
        var cursor: Int? = null
        do {
            val response = remoteDataSource.getGames(
                teamId = teamId,
                season = season,
                cursor = cursor,
                perPage = SEASON_PAGE_SIZE,
            )
            games += response.data
            cursor = response.meta?.nextCursor
        } while (cursor != null)

        return games
            .filter { it.statusState != SCHEDULED_STATE }
            .map { it.toDomain() }
            .sortedWith(compareByDescending<Game> { it.date }.thenByDescending { it.id })
    }
}
