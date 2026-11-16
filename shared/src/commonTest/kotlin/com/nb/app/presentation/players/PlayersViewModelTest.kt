package com.nb.app.presentation.players

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.domain.model.Page
import com.nb.app.domain.model.Player
import com.nb.app.testing.MainDispatcherTest
import com.nb.app.testing.TestData.warriors
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlayersViewModelTest : MainDispatcherTest(StandardTestDispatcher()) {

    private val repository = FakePlayerRepository()

    private fun player(id: Int, lastName: String = "Curry") = Player(id, "Player$id", lastName, warriors)

    private fun page(vararg ids: Int, nextCursor: Int?) =
        Result.success(Page(items = ids.map { player(it) }, nextCursor = nextCursor))

    /** Lets the debounce window pass and runs the search it triggers. */
    private fun TestScope.waitForDebounce() {
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)
        runCurrent()
    }

    @Test
    fun typing_searchesOnceAfterTheUserStops() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, 2, nextCursor = null)
        val viewModel = PlayersViewModel(repository)

        viewModel.onQueryChange("cu")
        advanceTimeBy(200)
        viewModel.onQueryChange("cur")
        advanceTimeBy(200)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        assertEquals(listOf<Pair<String, Int?>>("curry" to null), repository.requests)
        val state = viewModel.uiState.value
        assertEquals("curry", state.query)
        assertEquals(listOf(1, 2), state.players.map { it.id })
        assertTrue(state.hasSearched)
        assertTrue(state.endReached)
    }

    @Test
    fun queryIsTrimmedButShownAsTyped() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, nextCursor = null)
        val viewModel = PlayersViewModel(repository)

        viewModel.onQueryChange("  curry ")
        waitForDebounce()

        assertEquals(listOf<Pair<String, Int?>>("curry" to null), repository.requests)
        assertEquals("  curry ", viewModel.uiState.value.query)
    }

    @Test
    fun shortQuery_doesNotSearch() = runTest(testDispatcher) {
        val viewModel = PlayersViewModel(repository)

        viewModel.onQueryChange("c")
        waitForDebounce()

        assertTrue(repository.requests.isEmpty())
        assertFalse(viewModel.uiState.value.hasSearched)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun searchShowsLoadingUntilResultsArrive() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, nextCursor = null)
        val viewModel = PlayersViewModel(repository)

        val gate = CompletableDeferred<Unit>().also { repository.gate = it }

        viewModel.onQueryChange("curry")
        waitForDebounce()
        assertTrue(viewModel.uiState.value.isLoading)

        gate.complete(Unit)
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun noResults_isAnEmptySearch() = runTest(testDispatcher) {
        repository.results["zzz" to null] = Result.success(Page(emptyList<Player>(), nextCursor = null))
        val viewModel = PlayersViewModel(repository)

        viewModel.onQueryChange("zzz")
        waitForDebounce()

        val state = viewModel.uiState.value
        assertTrue(state.hasSearched)
        assertTrue(state.players.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun loadMore_appendsNextPageOfTheSameQuery() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, 2, nextCursor = 25)
        repository.results["curry" to 25] = page(3, nextCursor = null)
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        viewModel.onLoadMore()
        assertTrue(viewModel.uiState.value.isLoadingMore)
        viewModel.onLoadMore()
        runCurrent()

        assertEquals(listOf("curry" to null, "curry" to 25), repository.requests)
        assertEquals(listOf(1, 2, 3), viewModel.uiState.value.players.map { it.id })
        assertTrue(viewModel.uiState.value.endReached)
    }

    @Test
    fun newQuery_replacesResultsAndRestartsPaging() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, 2, nextCursor = 25)
        repository.results["james" to null] = page(7, nextCursor = 50)
        repository.results["james" to 50] = page(8, nextCursor = null)
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        viewModel.onQueryChange("james")
        waitForDebounce()
        viewModel.onLoadMore()
        runCurrent()

        assertEquals(listOf(7, 8), viewModel.uiState.value.players.map { it.id })
        assertEquals(listOf("curry" to null, "james" to null, "james" to 50), repository.requests)
    }

    @Test
    fun clearingQuery_clearsResults() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, nextCursor = null)
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        viewModel.onClearQueryClick()
        waitForDebounce()

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertTrue(state.players.isEmpty())
        assertFalse(state.hasSearched)
    }

    @Test
    fun searchFailure_showsErrorAndRetrySearchesAgain() = runTest(testDispatcher) {
        repository.results["curry" to null] = Result.failure(ApiException(ApiError.RATE_LIMITED))
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()
        assertEquals(ApiError.RATE_LIMITED, viewModel.uiState.value.error)

        repository.results["curry" to null] = page(1, nextCursor = null)
        viewModel.onRetryClick()
        runCurrent()

        assertNull(viewModel.uiState.value.error)
        assertEquals(listOf(1), viewModel.uiState.value.players.map { it.id })
    }

    @Test
    fun loadMoreFailure_keepsPlayersAndWaitsForRetry() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, nextCursor = 25)
        repository.results["curry" to 25] = Result.failure(ApiException(ApiError.NETWORK))
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        viewModel.onLoadMore()
        runCurrent()
        assertEquals(ApiError.NETWORK, viewModel.uiState.value.loadMoreError)
        assertEquals(listOf(1), viewModel.uiState.value.players.map { it.id })

        viewModel.onLoadMore()
        runCurrent()
        assertEquals(2, repository.requests.size)

        repository.results["curry" to 25] = page(2, nextCursor = null)
        viewModel.onRetryClick()
        runCurrent()

        assertNull(viewModel.uiState.value.loadMoreError)
        assertEquals(listOf(1, 2), viewModel.uiState.value.players.map { it.id })
    }
}
