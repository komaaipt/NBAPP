package com.nb.app.presentation.players

import androidx.compose.runtime.Immutable
import com.nb.app.core.network.ApiError
import com.nb.app.domain.model.Player

@Immutable
data class PlayersUiState(
    /** Text in the search field, exactly as typed. */
    val query: String = "",
    val players: List<Player> = emptyList(),
    /** `true` once a search ran for the current query, so "no results" can be told apart. */
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ApiError? = null,
    val loadMoreError: ApiError? = null,
    val endReached: Boolean = false,
)
