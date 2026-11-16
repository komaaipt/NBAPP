package com.nb.app.domain.repository

import com.nb.app.domain.model.Page
import com.nb.app.domain.model.Player

interface PlayerRepository {
    /**
     * Searches players whose first or last name matches [query].
     *
     * @param cursor `null` for the first page, otherwise [Page.nextCursor] from the previous page.
     */
    suspend fun searchPlayers(query: String, cursor: Int? = null): Result<Page<Player>>
}
