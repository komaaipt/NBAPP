package com.nb.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nb.app.core.ui.toApiError
import com.nb.app.domain.repository.TeamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val teamRepository: TeamRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTeams(forceRefresh = false)
    }

    fun onRetryClick() {
        loadTeams(forceRefresh = true)
    }

    fun onSortClick() {
        _uiState.update { it.copy(isSortDialogVisible = true) }
    }

    fun onSortDialogDismiss() {
        _uiState.update { it.copy(isSortDialogVisible = false) }
    }

    fun onSortOptionSelected(option: TeamSortOption) {
        _uiState.update { state ->
            state.copy(
                sortOption = option,
                teams = state.teams.sortedWith(option.comparator),
                isSortDialogVisible = false,
            )
        }
    }

    private fun loadTeams(forceRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            teamRepository.getTeams(forceRefresh)
                .onSuccess { teams ->
                    _uiState.update { state ->
                        state.copy(
                            teams = teams.sortedWith(state.sortOption.comparator),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.toApiError()) }
                }
        }
    }
}
