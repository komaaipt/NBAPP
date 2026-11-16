package com.nb.app.presentation.teamgames

import androidx.paging.testing.asSnapshot
import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page
import com.nb.app.presentation.teamgames.TeamGamesListItem.GameRow
import com.nb.app.presentation.teamgames.TeamGamesListItem.SeasonHeader
import com.nb.app.testing.MainDispatcherTest
import com.nb.app.testing.TestData.celtics
import com.nb.app.testing.TestData.hawks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TeamGamesViewModelTest : MainDispatcherTest() {

    private val repository = FakeGameRepository()

    private fun game(id: Int, season: Int) = Game(id, "$season-11-0$id", "Final", hawks, 100 + id, celtics, 90 + id, season)

    private fun page(vararg games: Game, nextSeason: Int?) = Result.success(Page(games.toList(), nextSeason))

    @Test
    fun firstPage_startsWithItsSeasonHeader() = runTest {
        repository.results[null] = page(game(3, 2026), game(2, 2026), nextSeason = null)
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        val items = viewModel.items.asSnapshot()

        assertEquals(
            listOf(SeasonHeader(2026), GameRow(game(3, 2026)), GameRow(game(2, 2026))),
            items,
        )
    }

    @Test
    fun scrolling_loadsPreviousSeasonsWithTheirHeaders() = runTest {
        repository.results[null] = page(game(3, 2026), game(2, 2026), nextSeason = 2025)
        repository.results[2025] = page(game(1, 2025), nextSeason = null)
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        val items = viewModel.items.asSnapshot { appendScrollWhile { true } }

        assertEquals(
            listOf(
                SeasonHeader(2026), GameRow(game(3, 2026)), GameRow(game(2, 2026)),
                SeasonHeader(2025), GameRow(game(1, 2025)),
            ),
            items,
        )
        assertEquals(listOf(null, 2025), repository.requestedSeasons)
    }

    @Test
    fun firstPageFailure_isReported() = runTest {
        repository.results[null] = Result.failure(ApiException(ApiError.NETWORK))
        val viewModel = TeamGamesViewModel(teamId = 1, gameRepository = repository)

        assertFails { viewModel.items.asSnapshot() }
    }
}
