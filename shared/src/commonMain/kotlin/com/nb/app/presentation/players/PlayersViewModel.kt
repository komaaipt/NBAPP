package com.nb.app.presentation.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nb.app.domain.model.Player
import com.nb.app.domain.repository.PlayerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal const val SEARCH_DEBOUNCE_MILLIS = 500L
internal const val MIN_QUERY_LENGTH = 2

private val NoSearchLoadStates = LoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = true),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)

internal val PlayersPagingConfig = PagingConfig(
    pageSize = 25,
    prefetchDistance = 5,
    initialLoadSize = 25,
    enablePlaceholders = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PlayersViewModel(
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchedQuery: StateFlow<String> = _query
        .map { it.trim() }
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = "")

    val players: Flow<PagingData<Player>> = searchedQuery
        .flatMapLatest { query ->
            if (query.length < MIN_QUERY_LENGTH) {
                flowOf(PagingData.empty(NoSearchLoadStates))
            } else {
                Pager(config = PlayersPagingConfig) { PlayersPagingSource(query, playerRepository) }.flow
            }
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onClearQueryClick() {
        _query.value = ""
    }
}
