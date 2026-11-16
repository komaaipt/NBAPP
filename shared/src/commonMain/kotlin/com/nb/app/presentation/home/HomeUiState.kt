package com.nb.app.presentation.home

import androidx.compose.runtime.Immutable
import com.nb.app.core.network.ApiError
import com.nb.app.domain.model.Team

@Immutable
data class HomeUiState(
    /** Teams already ordered by [sortOption]. */
    val teams: List<Team> = emptyList(),
    val sortOption: TeamSortOption = TeamSortOption.NAME,
    val isLoading: Boolean = false,
    val error: ApiError? = null,
    val isSortDialogVisible: Boolean = false,
)
