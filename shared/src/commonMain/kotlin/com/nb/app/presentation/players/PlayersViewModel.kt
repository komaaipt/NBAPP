package com.nb.app.presentation.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nb.app.core.ui.toApiError
import com.nb.app.domain.repository.PlayerRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Waits for the user to stop typing; the free API tier allows only 5 requests per minute. */
internal const val SEARCH_DEBOUNCE_MILLIS = 500L

/** Shorter queries match too many players to be useful. */
internal const val MIN_QUERY_LENGTH = 2

class PlayersViewModel(
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayersUiState())
    val uiState: StateFlow<PlayersUiState> = _uiState.asStateFlow()

    private val queryInput = MutableStateFlow("")
    private var searchedQuery = ""
    private var nextCursor: Int? = null
    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        observeQuery()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        queryInput.value = query
    }

    fun onClearQueryClick() {
        onQueryChange("")
    }

    /** Called by the list when the user scrolls close to the end. */
    fun onLoadMore() {
        val state = _uiState.value
        if (state.players.isEmpty() || state.endReached || state.loadMoreError != null) return
        loadMore()
    }

    fun onRetryClick() {
        if (_uiState.value.players.isEmpty()) search(searchedQuery) else loadMore()
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            queryInput
                .map { it.trim() }
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collect(::search)
        }
    }

    private fun search(query: String) {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        searchedQuery = query
        nextCursor = null

        if (query.length < MIN_QUERY_LENGTH) {
            _uiState.update { PlayersUiState(query = it.query) }
            return
        }

        _uiState.update {
            PlayersUiState(query = it.query, isLoading = true)
        }
        searchJob = viewModelScope.launch {
            playerRepository.searchPlayers(query)
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _uiState.update {
                        it.copy(
                            players = page.items,
                            hasSearched = true,
                            isLoading = false,
                            endReached = !page.hasMore,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.toApiError())
                    }
                }
        }
    }

    private fun loadMore() {
        if (loadMoreJob?.isActive == true || searchJob?.isActive == true) return
        val query = searchedQuery
        val cursor = nextCursor ?: return
        _uiState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
        loadMoreJob = viewModelScope.launch {
            playerRepository.searchPlayers(query, cursor)
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _uiState.update {
                        it.copy(
                            players = it.players + page.items,
                            isLoadingMore = false,
                            endReached = !page.hasMore,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoadingMore = false, loadMoreError = throwable.toApiError())
                    }
                }
        }
    }
}
