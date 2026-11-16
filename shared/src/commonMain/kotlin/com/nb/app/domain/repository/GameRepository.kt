package com.nb.app.domain.repository

import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page

interface GameRepository {
    /**
     * Returns one page of games played by the team with [teamId].
     *
     * @param cursor `null` for the first page, otherwise [Page.nextCursor] from the previous page.
     */
    suspend fun getTeamGames(teamId: Int, cursor: Int? = null): Result<Page<Game>>
}
