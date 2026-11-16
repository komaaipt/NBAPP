package com.nb.app.core.network

import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Runs [block] and wraps its outcome in a [Result], mapping every failure to an [ApiException].
 * Coroutine cancellation is always rethrown so structured concurrency keeps working.
 */
internal suspend inline fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e.toApiException())
    }

internal fun Throwable.toApiException(): ApiException = when (this) {
    is ApiException -> this
    is ResponseException -> ApiException(response.status.value.toApiError(), this)
    is ContentConvertException, is SerializationException -> ApiException(ApiError.SERIALIZATION, this)
    is IOException -> ApiException(ApiError.NETWORK, this)
    else -> ApiException(ApiError.UNKNOWN, this)
}

internal fun Int.toApiError(): ApiError = when (this) {
    401, 403 -> ApiError.UNAUTHORIZED
    404 -> ApiError.NOT_FOUND
    429 -> ApiError.RATE_LIMITED
    in 500..599 -> ApiError.SERVER
    else -> ApiError.UNKNOWN
}
