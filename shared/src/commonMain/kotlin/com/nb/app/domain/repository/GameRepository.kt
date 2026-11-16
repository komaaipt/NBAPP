package com.nb.app.domain.repository

import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page

interface GameRepository {
    /**
     * Returns the games a team has played in one season, most recent first.
     *
     * The API only lists games oldest first, so each page is a whole season: the first page is
     * the current season and [Page.nextCursor] is the previous season, or `null` when there are
     * no older games. Games not played yet are left out.
     *
     * @param season season to load (e.g. `2025` for 2025-26), or `null` for the current one.
     */
    suspend fun getTeamGames(teamId: Int, season: Int? = null): Result<Page<Game>>
}
