package com.nb.app.data.repository

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import com.nb.app.data.Fixtures
import com.nb.app.data.TEST_API_KEY
import com.nb.app.data.TestHttpClient
import com.nb.app.data.remote.NbaRemoteDataSource
import com.nb.app.data.respondJson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TeamRepositoryImplTest {

    @Test
    fun getTeams_mapsResponseAndSendsApiKey() = runTest {
        val http = TestHttpClient { respondJson(Fixtures.TEAMS_JSON) }
        val repository = TeamRepositoryImpl(NbaRemoteDataSource(http.client))

        val teams = repository.getTeams().getOrThrow()

        assertEquals(2, teams.size)
        val hawks = teams.first()
        assertEquals(1, hawks.id)
        assertEquals("Atlanta Hawks", hawks.fullName)
        assertEquals("Atlanta", hawks.city)
        assertEquals("East", hawks.conference)

        val request = http.requests.single()
        assertEquals("/v1/teams", request.url.encodedPath)
        assertEquals(TEST_API_KEY, request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun getTeams_usesCacheUntilForceRefresh() = runTest {
        val http = TestHttpClient { respondJson(Fixtures.TEAMS_JSON) }
        val repository = TeamRepositoryImpl(NbaRemoteDataSource(http.client))

        repository.getTeams()
        repository.getTeams()
        assertEquals(1, http.requests.size)

        repository.getTeams(forceRefresh = true)
        assertEquals(2, http.requests.size)
    }

    @Test
    fun getTeams_doesNotCacheFailures() = runTest {
        var callCount = 0
        val http = TestHttpClient {
            callCount++
            if (callCount == 1) {
                respondJson("""{"error":"boom"}""", HttpStatusCode.InternalServerError)
            } else {
                respondJson(Fixtures.TEAMS_JSON)
            }
        }
        val repository = TeamRepositoryImpl(NbaRemoteDataSource(http.client))

        val failure = repository.getTeams().exceptionOrNull()
        assertIs<ApiException>(failure)
        assertEquals(ApiError.SERVER, failure.error)

        assertEquals(2, repository.getTeams().getOrThrow().size)
    }

    @Test
    fun getTeams_mapsUnauthorized() = runTest {
        val http = TestHttpClient { respondJson("Unauthorized", HttpStatusCode.Unauthorized) }
        val repository = TeamRepositoryImpl(NbaRemoteDataSource(http.client))

        val failure = repository.getTeams().exceptionOrNull()

        assertIs<ApiException>(failure)
        assertEquals(ApiError.UNAUTHORIZED, failure.error)
    }
}
