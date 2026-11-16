package com.nb.app.presentation.teamgames

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page
import com.nb.app.testing.MainDispatcherTest
import com.nb.app.testing.TestData.celtics
import com.nb.app.testing.TestData.hawks
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TeamGamesViewModelTest : MainDispatcherTest() {

    private val repository = FakeGameRepository()

    private fun game(id: Int) = Game(id, "2026-11-0$id", "Final", hawks, 100 + id, celtics, 90 + id)

    private fun page(vararg ids: Int, nextSeason: Int?) =
        Result.success(Page(items = ids.map(::game), nextCursor = nextSeason))

    @Test
    fun init_loadsCurrentSeason() {
        repository.results[null] = page(3, 2, nextSeason = 2025)

        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        val state = viewModel.uiState.value
        assertEquals(listOf(3, 2), state.games.map { it.id })
        assertFalse(state.isLoading)
        assertFalse(state.endReached)
        assertEquals(listOf<Int?>(null), repository.requestedSeasons)
    }

    @Test
    fun init_showsLoadingUntilFirstPageArrives() {
        repository.results[null] = page(1, nextSeason = null)
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }

        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)
        assertTrue(viewModel.uiState.value.isLoading)

        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadMore_appendsPreviousSeason() {
        repository.results[null] = page(3, 2, nextSeason = 2025)
        repository.results[2025] = page(1, nextSeason = 2024)
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        viewModel.onLoadMore()

        assertEquals(listOf(3, 2, 1), viewModel.uiState.value.games.map { it.id })
        assertEquals(listOf(null, 2025), repository.requestedSeasons)
        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    @Test
    fun loadMore_whileLoading_isIgnored() {
        repository.results[null] = page(2, nextSeason = 2025)
        repository.results[2025] = page(1, nextSeason = 2024)
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)
        val gate = CompletableDeferred<Unit>().also { repository.gate = it }

        viewModel.onLoadMore()
        assertTrue(viewModel.uiState.value.isLoadingMore)
        viewModel.onLoadMore()
        viewModel.onLoadMore()
        gate.complete(Unit)

        assertEquals(listOf(null, 2025), repository.requestedSeasons)
    }

    @Test
    fun lastPage_marksEndReachedAndStopsLoading() {
        repository.results[null] = page(1, nextSeason = null)
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        viewModel.onLoadMore()

        assertTrue(viewModel.uiState.value.endReached)
        assertEquals(listOf<Int?>(null), repository.requestedSeasons)
    }

    @Test
    fun emptyFirstPage_isAnEmptyList() {
        repository.results[null] = Result.success(Page(items = emptyList(), nextCursor = null))

        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        val state = viewModel.uiState.value
        assertTrue(state.games.isEmpty())
        assertTrue(state.endReached)
        assertNull(state.error)
    }

    @Test
    fun firstPageFailure_showsErrorAndRetryReloadsIt() {
        repository.results[null] = Result.failure(ApiException(ApiError.NETWORK))
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)
        assertEquals(ApiError.NETWORK, viewModel.uiState.value.error)

        repository.results[null] = page(1, nextSeason = 2025)
        viewModel.onRetryClick()

        assertNull(viewModel.uiState.value.error)
        assertEquals(listOf(1), viewModel.uiState.value.games.map { it.id })
        assertEquals(listOf<Int?>(null, null), repository.requestedSeasons)
    }

    @Test
    fun loadMoreFailure_keepsGamesAndWaitsForRetry() {
        repository.results[null] = page(2, nextSeason = 2025)
        repository.results[2025] = Result.failure(ApiException(ApiError.RATE_LIMITED))
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        viewModel.onLoadMore()
        val failed = viewModel.uiState.value
        assertEquals(ApiError.RATE_LIMITED, failed.loadMoreError)
        assertNull(failed.error)
        assertEquals(listOf(2), failed.games.map { it.id })

        // Scrolling again doesn't hit the API until the user taps retry.
        viewModel.onLoadMore()
        assertEquals(listOf(null, 2025), repository.requestedSeasons)

        repository.results[2025] = page(1, nextSeason = 2024)
        viewModel.onRetryClick()

        assertNull(viewModel.uiState.value.loadMoreError)
        assertEquals(listOf(2, 1), viewModel.uiState.value.games.map { it.id })
        assertEquals(listOf(null, 2025, 2025), repository.requestedSeasons)
    }
}
