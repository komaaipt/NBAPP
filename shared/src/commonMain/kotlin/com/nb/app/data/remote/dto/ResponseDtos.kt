package com.nb.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Envelope used by every list endpoint of the balldontlie API.
 * [meta] is only sent by paginated endpoints.
 */
@Serializable
data class ListResponseDto<T>(
    @SerialName("data") val data: List<T>,
    @SerialName("meta") val meta: MetaDto? = null,
)

@Serializable
data class MetaDto(
    @SerialName("next_cursor") val nextCursor: Int? = null,
    @SerialName("per_page") val perPage: Int? = null,
)
