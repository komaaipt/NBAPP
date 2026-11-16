package com.nb.app.presentation.teamgames

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nb.app.domain.model.Game
import com.nb.app.domain.repository.GameRepository

/** One page per season, newest first. The key is the season to load; `null` is the current one. */
class TeamGamesPagingSource(
    private val teamId: Int,
    private val gameRepository: GameRepository,
) : PagingSource<Int, Game>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Game> =
        gameRepository.getTeamGames(teamId = teamId, season = params.key).fold(
            onSuccess = { page -> LoadResult.Page(data = page.items, prevKey = null, nextKey = page.nextCursor) },
            onFailure = { LoadResult.Error(it) },
        )

    override fun getRefreshKey(state: PagingState<Int, Game>): Int? = null
}
