package com.nb.app.presentation.players

import androidx.paging.testing.asSnapshot
import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.domain.model.Page
import com.nb.app.domain.model.Player
import com.nb.app.testing.MainDispatcherTest
import com.nb.app.testing.TestData.warriors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlayersViewModelTest : MainDispatcherTest(StandardTestDispatcher()) {

    private val repository = FakePlayerRepository()

    private fun player(id: Int) = Player(id, "Player$id", "Curry", warriors)

    private fun page(vararg ids: Int, nextCursor: Int?) =
        Result.success(Page(items = ids.map(::player), nextCursor = nextCursor))

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

        assertEquals("curry", viewModel.searchedQuery.value)
        assertEquals(listOf(1, 2), viewModel.players.asSnapshot().map { it.id })
        assertEquals(listOf<Pair<String, Int?>>("curry" to null), repository.requests)
    }

    @Test
    fun queryIsTrimmedForTheSearchButKeptAsTyped() = runTest(testDispatcher) {
        val viewModel = PlayersViewModel(repository)

        viewModel.onQueryChange("  curry ")
        waitForDebounce()

        assertEquals("  curry ", viewModel.query.value)
        assertEquals("curry", viewModel.searchedQuery.value)
    }

    @Test
    fun shortQuery_doesNotSearch() = runTest(testDispatcher) {
        val viewModel = PlayersViewModel(repository)

        viewModel.onQueryChange("c")
        waitForDebounce()

        assertTrue(viewModel.players.asSnapshot().isEmpty())
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun scrolling_loadsNextPagesOfTheSameQuery() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, 2, nextCursor = 25)
        repository.results["curry" to 25] = page(3, nextCursor = null)
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        val players = viewModel.players.asSnapshot { appendScrollWhile { true } }

        assertEquals(listOf(1, 2, 3), players.map { it.id })
        assertEquals(listOf<Pair<String, Int?>>("curry" to null, "curry" to 25), repository.requests)
    }

    @Test
    fun newQuery_replacesTheResults() = runTest(testDispatcher) {
        repository.results["curry" to null] = page(1, nextCursor = null)
        repository.results["james" to null] = page(7, nextCursor = null)
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        viewModel.onQueryChange("james")
        waitForDebounce()

        assertEquals(listOf(7), viewModel.players.asSnapshot().map { it.id })
    }

    @Test
    fun clearingTheQuery_clearsTheSearch() = runTest(testDispatcher) {
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        viewModel.onClearQueryClick()
        waitForDebounce()

        assertEquals("", viewModel.query.value)
        assertEquals("", viewModel.searchedQuery.value)
    }

    @Test
    fun searchFailure_isReported() = runTest(testDispatcher) {
        repository.results["curry" to null] = Result.failure(ApiException(ApiError.RATE_LIMITED))
        val viewModel = PlayersViewModel(repository)
        viewModel.onQueryChange("curry")
        waitForDebounce()

        assertFails { viewModel.players.asSnapshot() }
    }
}
