package com.nb.app.presentation.teamgames

import androidx.compose.runtime.Immutable
import com.nb.app.core.network.ApiError
import com.nb.app.domain.model.Game

@Immutable
data class TeamGamesUiState(
    val games: List<Game> = emptyList(),
    /** Loading the first page. */
    val isLoading: Boolean = false,
    /** Loading the next page while the current games stay on screen. */
    val isLoadingMore: Boolean = false,
    /** The first page failed, so there is nothing to show. */
    val error: ApiError? = null,
    /** A later page failed; the loaded games stay on screen. */
    val loadMoreError: ApiError? = null,
    val endReached: Boolean = false,
)
