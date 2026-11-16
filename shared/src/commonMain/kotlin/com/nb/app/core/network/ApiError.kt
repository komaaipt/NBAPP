package com.nb.app.core.network

/**
 * Failures the data layer can report to the rest of the app.
 */
enum class ApiError {
    /** No connection, timeout or any other I/O problem. */
    NETWORK,

    /** HTTP 401: missing/invalid API key or the tier has no access to the endpoint. */
    UNAUTHORIZED,

    /** HTTP 429: the API rate limit was hit. */
    RATE_LIMITED,

    /** HTTP 404. */
    NOT_FOUND,

    /** HTTP 5xx. */
    SERVER,

    /** The response body could not be parsed. */
    SERIALIZATION,

    /** Anything else. */
    UNKNOWN,
}

class ApiException(
    val error: ApiError,
    cause: Throwable? = null,
) : Exception("API request failed: $error", cause)
