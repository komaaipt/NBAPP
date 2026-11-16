package com.nb.app.presentation.teamgames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nb.app.core.ui.toApiError
import com.nb.app.domain.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Games of the team with [teamId], loaded page by page (one season per page) for endless scroll.
 */
class TeamGamesViewModel(
    private val teamId: Int,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamGamesUiState())
    val uiState: StateFlow<TeamGamesUiState> = _uiState.asStateFlow()

    /** Season of the next page; `null` before the first page loads. */
    private var nextSeason: Int? = null
    private var loadJob: Job? = null

    init {
        loadPage(isFirstPage = true)
    }

    /** Called by the list when the user scrolls close to the end. */
    fun onLoadMore() {
        val state = _uiState.value
        // A failed page waits for an explicit retry instead of hitting the API again on scroll.
        if (state.games.isEmpty() || state.endReached || state.loadMoreError != null) return
        loadPage(isFirstPage = false)
    }

    fun onRetryClick() {
        loadPage(isFirstPage = _uiState.value.games.isEmpty())
    }

    private fun loadPage(isFirstPage: Boolean) {
        if (loadJob?.isActive == true) return
        _uiState.update {
            it.copy(
                isLoading = isFirstPage,
                isLoadingMore = !isFirstPage,
                error = null,
                loadMoreError = null,
            )
        }
        loadJob = viewModelScope.launch {
            val season = if (isFirstPage) null else nextSeason
            gameRepository.getTeamGames(teamId = teamId, season = season)
                .onSuccess { page ->
                    nextSeason = page.nextCursor
                    _uiState.update {
                        it.copy(
                            games = it.games + page.items,
                            isLoading = false,
                            isLoadingMore = false,
                            endReached = !page.hasMore,
                        )
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.toApiError()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = if (isFirstPage) error else null,
                            loadMoreError = if (isFirstPage) null else error,
                        )
                    }
                }
        }
    }
}
