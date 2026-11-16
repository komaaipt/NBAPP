package com.nb.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val REQUEST_TIMEOUT_MILLIS = 15_000L

internal val nbaJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

/**
 * Builds the [HttpClient] used to talk to the balldontlie API.
 *
 * The engine is injected so tests can pass a `MockEngine`.
 */
internal fun createHttpClient(
    engine: HttpClientEngine,
    baseUrl: String = ApiConfig.BASE_URL,
    apiKey: String = ApiConfig.API_KEY,
    json: Json = nbaJson,
    enableLogging: Boolean = false,
): HttpClient = HttpClient(engine) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
    if (enableLogging) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
    defaultRequest {
        url(baseUrl)
        accept(ContentType.Application.Json)
        if (apiKey.isNotBlank()) {
            headers.append(HttpHeaders.Authorization, apiKey)
        }
    }
}
