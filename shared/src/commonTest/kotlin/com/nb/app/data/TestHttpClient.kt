package com.nb.app.data

import com.nb.app.core.network.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

internal const val TEST_BASE_URL = "https://api.test/v1/"
internal const val TEST_API_KEY = "test-api-key"

/**
 * Creates the production [HttpClient] configuration on top of a [MockEngine] and records every
 * request it receives in [requests].
 */
internal class TestHttpClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
) {
    val requests = mutableListOf<HttpRequestData>()

    val client: HttpClient = createHttpClient(
        engine = MockEngine { request ->
            requests += request
            handler(request)
        },
        baseUrl = TEST_BASE_URL,
        apiKey = TEST_API_KEY,
    )
}

internal fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = body,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)
