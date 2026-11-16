package com.nb.app.domain.model

/**
 * One page of a cursor-paginated list.
 *
 * @property nextCursor cursor for the next page, or `null` when this is the last page.
 */
data class Page<T>(
    val items: List<T>,
    val nextCursor: Int?,
) {
    val hasMore: Boolean get() = nextCursor != null
}
