package com.nb.app.domain.model

data class Game(
    val id: Int,
    val date: String,
    val status: String,
    val homeTeam: Team,
    val homeTeamScore: Int,
    val visitorTeam: Team,
    val visitorTeamScore: Int,
)
