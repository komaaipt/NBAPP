package com.nb.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    @SerialName("id") val id: Int,
    @SerialName("date") val date: String = "",
    @SerialName("season") val season: Int? = null,
    @SerialName("status") val status: String = "",
    @SerialName("postseason") val postseason: Boolean = false,
    @SerialName("home_team") val homeTeam: TeamDto,
    @SerialName("home_team_score") val homeTeamScore: Int = 0,
    @SerialName("visitor_team") val visitorTeam: TeamDto,
    @SerialName("visitor_team_score") val visitorTeamScore: Int = 0,
)
