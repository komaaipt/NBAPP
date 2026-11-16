package com.nb.app.presentation.players

import androidx.compose.runtime.Immutable
import com.nb.app.core.network.ApiError
import com.nb.app.domain.model.Player

@Immutable
data class PlayersUiState(
    val query: String = "",
    val players: List<Player> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ApiError? = null,
    val loadMoreError: ApiError? = null,
    val endReached: Boolean = false,
)
