package com.nb.app.presentation.teamgames

import com.nb.app.domain.model.Game

sealed interface TeamGamesListItem {
    val key: String

    data class SeasonHeader(val season: Int) : TeamGamesListItem {
        override val key = "season_$season"
    }

    data class GameRow(val game: Game) : TeamGamesListItem {
        override val key = "game_${game.id}"
    }
}
