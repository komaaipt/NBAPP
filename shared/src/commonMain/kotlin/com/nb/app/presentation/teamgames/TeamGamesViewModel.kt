package com.nb.app.presentation.teamgames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.nb.app.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val TeamGamesPagingConfig = PagingConfig(
    pageSize = 100,
    prefetchDistance = 5,
    initialLoadSize = 100,
    enablePlaceholders = false,
)

class TeamGamesViewModel(
    private val teamId: Int,
    private val gameRepository: GameRepository,
) : ViewModel() {

    val items: Flow<PagingData<TeamGamesListItem>> =
        Pager(config = TeamGamesPagingConfig) { TeamGamesPagingSource(teamId, gameRepository) }
            .flow
            .map { pagingData ->
                pagingData
                    .map { TeamGamesListItem.GameRow(it) }
                    .insertSeparators { before: TeamGamesListItem.GameRow?, after: TeamGamesListItem.GameRow? ->
                        val season = after?.game?.season
                        if (season != null && season != before?.game?.season) {
                            TeamGamesListItem.SeasonHeader(season)
                        } else {
                            null
                        }
                    }
            }
            .cachedIn(viewModelScope)
}
