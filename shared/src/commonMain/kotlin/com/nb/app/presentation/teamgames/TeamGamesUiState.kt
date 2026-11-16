package com.nb.app.presentation.teamgames

import androidx.compose.runtime.Immutable
import com.nb.app.core.network.ApiError
import com.nb.app.domain.model.Game

@Immutable
data class TeamGamesUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ApiError? = null,
    val loadMoreError: ApiError? = null,
    val endReached: Boolean = false,
)
