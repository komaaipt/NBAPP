package com.nb.app.data.repository

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.data.Fixtures
import com.nb.app.data.TestHttpClient
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.data.respondJson
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerRepositoryImplTest {

    @Test
    fun searchPlayers_sendsTrimmedQueryAndMapsPlayers() = runTest {
        val http = TestHttpClient { respondJson(Fixtures.PLAYERS_JSON) }
        val repository = PlayerRepositoryImpl(NbaRemoteDataSource(http.client))

        val page = repository.searchPlayers("  curry ").getOrThrow()

        val url = http.requests.single().url
        assertEquals("/v1/players", url.encodedPath)
        assertEquals("curry", url.parameters["search"])

        assertEquals(25, page.nextCursor)
        val curry = page.items.first()
        assertEquals("Stephen", curry.firstName)
        assertEquals("Curry", curry.lastName)
        assertEquals("Golden State Warriors", curry.team?.fullName)
        assertNull(page.items.last().team)
    }

    @Test
    fun searchPlayers_blankQuery_returnsEmptyPageWithoutRequest() = runTest {
        val http = TestHttpClient { error("No request expected") }
        val repository = PlayerRepositoryImpl(NbaRemoteDataSource(http.client))

        val page = repository.searchPlayers("   ").getOrThrow()

        assertTrue(page.items.isEmpty())
        assertFalse(page.hasMore)
        assertTrue(http.requests.isEmpty())
    }

    @Test
    fun searchPlayers_mapsMalformedJsonToSerializationError() = runTest {
        val http = TestHttpClient { respondJson("""{"data": "not a list"}""") }
        val repository = PlayerRepositoryImpl(NbaRemoteDataSource(http.client))

        val failure = repository.searchPlayers("curry").exceptionOrNull()

        assertIs<ApiException>(failure)
        assertEquals(ApiError.SERIALIZATION, failure.error)
    }

    @Test
    fun searchPlayers_mapsIoFailureToNetworkError() = runTest {
        val http = TestHttpClient { throw IOException("offline") }
        val repository = PlayerRepositoryImpl(NbaRemoteDataSource(http.client))

        val failure = repository.searchPlayers("curry").exceptionOrNull()

        assertIs<ApiException>(failure)
        assertEquals(ApiError.NETWORK, failure.error)
    }
}
