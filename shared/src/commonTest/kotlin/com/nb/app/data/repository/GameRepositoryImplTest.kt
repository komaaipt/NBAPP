package com.nb.app.data.repository

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.data.Fixtures
import com.nb.app.data.Fixtures.GameRow
import com.nb.app.data.TestHttpClient
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.data.respondJson
import com.nb.app.testing.FakeClock
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class GameRepositoryImplTest {

    private val clock = FakeClock()

    private fun repository(http: TestHttpClient, currentSeason: Int = 2026) =
        GameRepositoryImpl(NbaRemoteDataSource(http.client), currentSeason = { currentSeason }, clock = clock)

    private fun seasonResponder() = TestHttpClient {
        respondJson(Fixtures.gamesJson(listOf(GameRow(id = 1, date = "2026-11-01"))))
    }

    @Test
    fun gamesOfASeason_shareOneInstancePerTeam() = runTest {
        val http = TestHttpClient {
            respondJson(
                Fixtures.gamesJson(
                    listOf(GameRow(id = 1, date = "2026-11-01"), GameRow(id = 2, date = "2026-11-03")),
                ),
            )
        }

        val games = repository(http).getTeamGames(teamId = 6, season = 2026).getOrThrow().items

        assertSame(games[0].homeTeam, games[1].homeTeam)
        assertSame(games[0].visitorTeam, games[1].visitorTeam)
    }

    @Test
    fun sameSeasonTwice_callsTheApiOnce() = runTest {
        val http = seasonResponder()
        val repository = repository(http)

        repository.getTeamGames(teamId = 6, season = 2024)
        repository.getTeamGames(teamId = 6, season = 2024)
        repository.getTeamGames(teamId = 7, season = 2024)

        assertEquals(2, http.requests.size)
    }

    @Test
    fun currentSeason_expiresAfterFiveMinutes() = runTest {
        val http = seasonResponder()
        val repository = repository(http)

        repository.getTeamGames(teamId = 6)
        clock.advanceBy(4.minutes)
        repository.getTeamGames(teamId = 6)
        assertEquals(1, http.requests.size)

        clock.advanceBy(2.minutes)
        repository.getTeamGames(teamId = 6)
        assertEquals(2, http.requests.size)
    }

    @Test
    fun pastSeason_neverExpires() = runTest {
        val http = seasonResponder()
        val repository = repository(http)

        repository.getTeamGames(teamId = 6, season = 2020)
        clock.advanceBy(10.hours)
        repository.getTeamGames(teamId = 6, season = 2020)

        assertEquals(1, http.requests.size)
    }

    @Test
    fun failure_isNotCached() = runTest {
        var calls = 0
        val http = TestHttpClient {
            calls++
            if (calls == 1) {
                respondJson("Too Many Requests", HttpStatusCode.TooManyRequests)
            } else {
                respondJson(Fixtures.gamesJson(listOf(GameRow(id = 1, date = "2026-11-01"))))
            }
        }
        val repository = repository(http)

        assertTrue(repository.getTeamGames(teamId = 6, season = 2024).isFailure)
        assertTrue(repository.getTeamGames(teamId = 6, season = 2024).isSuccess)
        assertEquals(2, http.requests.size)
    }

    @Test
    fun firstPage_loadsCurrentSeasonNewestFirstWithoutScheduledGames() = runTest {
        val http = TestHttpClient {
            respondJson(
                Fixtures.gamesJson(
                    listOf(
                        GameRow(id = 1, date = "2026-10-21"),
                        GameRow(id = 2, date = "2026-10-24", homeScore = 118, visitorScore = 138),
                        GameRow(id = 3, date = "2026-11-20", statusState = "scheduled"),
                    ),
                ),
            )
        }

        val page = repository(http).getTeamGames(teamId = 6).getOrThrow()

        val url = http.requests.single().url
        assertEquals("/v1/games", url.encodedPath)
        assertEquals("6", url.parameters["team_ids[]"])
        assertEquals("2026", url.parameters["seasons[]"])
        assertEquals("100", url.parameters["per_page"])
        assertNull(url.parameters["cursor"])

        assertEquals(listOf(2, 1), page.items.map { it.id })
        val latest = page.items.first()
        assertEquals("Cleveland Cavaliers", latest.homeTeam.fullName)
        assertEquals(118, latest.homeTeamScore)
        assertEquals("Charlotte Hornets", latest.visitorTeam.fullName)
        assertEquals(138, latest.visitorTeamScore)
        assertEquals(2025, page.nextCursor)
    }

    @Test
    fun season_followsApiCursorUntilTheWholeSeasonIsLoaded() = runTest {
        val http = TestHttpClient { request ->
            when (request.url.parameters["cursor"]) {
                null -> respondJson(
                    Fixtures.gamesJson(listOf(GameRow(id = 1, date = "2025-10-22")), nextCursor = 77),
                )
                "77" -> respondJson(Fixtures.gamesJson(listOf(GameRow(id = 2, date = "2026-04-30"))))
                else -> error("Unexpected cursor")
            }
        }

        val page = repository(http).getTeamGames(teamId = 6, season = 2025).getOrThrow()

        assertEquals(2, http.requests.size)
        assertEquals(listOf("2025", "2025"), http.requests.map { it.url.parameters["seasons[]"] })
        assertEquals(listOf(2, 1), page.items.map { it.id })
        assertEquals(2024, page.nextCursor)
    }

    @Test
    fun firstPage_beforeTipOff_fallsBackToPreviousSeason() = runTest {
        val http = TestHttpClient { request ->
            when (request.url.parameters["seasons[]"]) {
                "2026" -> respondJson(
                    Fixtures.gamesJson(listOf(GameRow(id = 9, date = "2026-10-21", statusState = "scheduled"))),
                )
                "2025" -> respondJson(Fixtures.gamesJson(listOf(GameRow(id = 1, date = "2026-04-12"))))
                else -> error("Unexpected season")
            }
        }

        val page = repository(http).getTeamGames(teamId = 6).getOrThrow()

        assertEquals(listOf(1), page.items.map { it.id })
        assertEquals(2024, page.nextCursor)
    }

    @Test
    fun emptyOlderSeason_endsTheList() = runTest {
        val http = TestHttpClient { respondJson(Fixtures.gamesJson(emptyList())) }

        val page = repository(http).getTeamGames(teamId = 41, season = 2001).getOrThrow()

        assertEquals(1, http.requests.size)
        assertEquals(emptyList(), page.items)
        assertNull(page.nextCursor)
        assertFalse(page.hasMore)
    }

    @Test
    fun firstNbaSeason_isTheLastPage() = runTest {
        val http = TestHttpClient { respondJson(Fixtures.gamesJson(listOf(GameRow(id = 1, date = "1946-11-01")))) }

        val page = repository(http).getTeamGames(teamId = 2, season = FIRST_NBA_SEASON).getOrThrow()

        assertEquals(1, page.items.size)
        assertNull(page.nextCursor)
    }

    @Test
    fun rateLimit_isMapped() = runTest {
        val http = TestHttpClient { respondJson("Too Many Requests", HttpStatusCode.TooManyRequests) }

        val failure = repository(http).getTeamGames(teamId = 6).exceptionOrNull()

        assertIs<ApiException>(failure)
        assertEquals(ApiError.RATE_LIMITED, failure.error)
    }
}
