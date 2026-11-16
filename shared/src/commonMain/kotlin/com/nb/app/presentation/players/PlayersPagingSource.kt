package com.nb.app.presentation.players

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nb.app.domain.model.Player
import com.nb.app.domain.repository.PlayerRepository

class PlayersPagingSource(
    private val query: String,
    private val playerRepository: PlayerRepository,
) : PagingSource<Int, Player>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Player> =
        playerRepository.searchPlayers(query = query, cursor = params.key).fold(
            onSuccess = { page -> LoadResult.Page(data = page.items, prevKey = null, nextKey = page.nextCursor) },
            onFailure = { LoadResult.Error(it) },
        )

    override fun getRefreshKey(state: PagingState<Int, Player>): Int? = null
}
