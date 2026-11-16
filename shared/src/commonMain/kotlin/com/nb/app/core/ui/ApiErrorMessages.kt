package com.nb.app.core.ui

import com.nb.app.core.network.ApiError
import com.nb.app.core.network.ApiException
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.error_network
import nbapp.shared.generated.resources.error_not_found
import nbapp.shared.generated.resources.error_rate_limited
import nbapp.shared.generated.resources.error_serialization
import nbapp.shared.generated.resources.error_server
import nbapp.shared.generated.resources.error_unauthorized
import nbapp.shared.generated.resources.error_unknown
import org.jetbrains.compose.resources.StringResource

/** User-facing message for an [ApiError]. */
fun ApiError.toMessage(): StringResource = when (this) {
    ApiError.NETWORK -> Res.string.error_network
    ApiError.UNAUTHORIZED -> Res.string.error_unauthorized
    ApiError.RATE_LIMITED -> Res.string.error_rate_limited
    ApiError.NOT_FOUND -> Res.string.error_not_found
    ApiError.SERVER -> Res.string.error_server
    ApiError.SERIALIZATION -> Res.string.error_serialization
    ApiError.UNKNOWN -> Res.string.error_unknown
}

/** Maps any failure coming from a repository to an [ApiError]. */
fun Throwable.toApiError(): ApiError = (this as? ApiException)?.error ?: ApiError.UNKNOWN
