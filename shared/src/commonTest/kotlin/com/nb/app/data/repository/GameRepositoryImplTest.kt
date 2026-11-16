package com.nb.app.data.repository

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.data.Fixtures
import com.nb.app.data.TestHttpClient
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.data.respondJson
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameRepositoryImplTest {

    @Test
    fun getTeamGames_firstPage_sendsTeamIdAndPageSize() = runTest {
        val http = TestHttpClient { respondJson(Fixtures.gamesJson(nextCursor = 25)) }
        val repository = GameRepositoryImpl(NbaRemoteDataSource(http.client), pageSize = 25)

        val page = repository.getTeamGames(teamId = 6).getOrThrow()

        val url = http.requests.single().url
        assertEquals("/v1/games", url.encodedPath)
        assertEquals("6", url.parameters["team_ids[]"])
        assertEquals("25", url.parameters["per_page"])
        assertNull(url.parameters["cursor"])

        assertEquals(25, page.nextCursor)
        assertTrue(page.hasMore)
        val game = page.items.single()
        assertEquals("Cleveland Cavaliers", game.homeTeam.fullName)
        assertEquals(115, game.homeTeamScore)
        assertEquals("Charlotte Hornets", game.visitorTeam.fullName)
        assertEquals(105, game.visitorTeamScore)
    }

    @Test
    fun getTeamGames_nextPage_sendsCursor() = runTest {
        val http = TestHttpClient { respondJson(Fixtures.gamesJson(nextCursor = null)) }
        val repository = GameRepositoryImpl(NbaRemoteDataSource(http.client))

        val page = repository.getTeamGames(teamId = 6, cursor = 25).getOrThrow()

        assertEquals("25", http.requests.single().url.parameters["cursor"])
        assertNull(page.nextCursor)
        assertFalse(page.hasMore)
    }

    @Test
    fun getTeamGames_mapsRateLimit() = runTest {
        val http = TestHttpClient { respondJson("Too Many Requests", HttpStatusCode.TooManyRequests) }
        val repository = GameRepositoryImpl(NbaRemoteDataSource(http.client))

        val failure = repository.getTeamGames(teamId = 6).exceptionOrNull()

        assertIs<ApiException>(failure)
        assertEquals(ApiError.RATE_LIMITED, failure.error)
    }
}
