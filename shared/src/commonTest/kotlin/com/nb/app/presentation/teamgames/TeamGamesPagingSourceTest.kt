package com.nb.app.presentation.teamgames

import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page
import com.nb.app.testing.TestData.celtics
import com.nb.app.testing.TestData.hawks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TeamGamesPagingSourceTest {

    private val repository = FakeGameRepository()
    private val pager = TestPager(TeamGamesPagingConfig, TeamGamesPagingSource(teamId = 1, gameRepository = repository))

    private fun game(id: Int, season: Int) = Game(id, "$season-11-01", "Final", hawks, 100, celtics, 90, season)

    @Test
    fun refresh_loadsCurrentSeasonAndPointsToThePreviousOne() = runTest {
        repository.results[null] = Result.success(Page(listOf(game(3, 2026), game(2, 2026)), nextCursor = 2025))

        val page = assertIs<LoadResult.Page<Int, Game>>(pager.refresh())

        assertEquals(listOf(3, 2), page.data.map { it.id })
        assertEquals(2025, page.nextKey)
        assertNull(page.prevKey)
    }

    @Test
    fun append_loadsTheNextSeason() = runTest {
        repository.results[null] = Result.success(Page(listOf(game(2, 2026)), nextCursor = 2025))
        repository.results[2025] = Result.success(Page(listOf(game(1, 2025)), nextCursor = null))
        pager.refresh()

        val page = assertIs<LoadResult.Page<Int, Game>>(pager.append())

        assertEquals(listOf(1), page.data.map { it.id })
        assertNull(page.nextKey)
        assertEquals(listOf(null, 2025), repository.requestedSeasons)
    }

    @Test
    fun repositoryFailure_isALoadError() = runTest {
        repository.results[null] = Result.failure(ApiException(ApiError.RATE_LIMITED))

        val error = assertIs<LoadResult.Error<Int, Game>>(pager.refresh())

        assertEquals(ApiError.RATE_LIMITED, assertIs<ApiException>(error.throwable).error)
    }
}
