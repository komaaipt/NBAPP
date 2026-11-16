package com.nb.app.data.mapper

import com.nb.app.data.remote.dto.GameDto
import com.nb.app.data.remote.dto.ListResponseDto
import com.nb.app.data.remote.dto.PlayerDto
import com.nb.app.data.remote.dto.TeamDto
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Page
import com.nb.app.domain.model.Player
import com.nb.app.domain.model.Team

internal fun TeamDto.toDomain(): Team = Team(
    id = id,
    fullName = fullName,
    name = name,
    city = city,
    conference = conference,
    abbreviation = abbreviation,
)

internal fun GameDto.toDomain(): Game = Game(
    id = id,
    date = date,
    status = status,
    homeTeam = homeTeam.toDomain(),
    homeTeamScore = homeTeamScore,
    visitorTeam = visitorTeam.toDomain(),
    visitorTeamScore = visitorTeamScore,
    season = season ?: 0,
)

internal fun PlayerDto.toDomain(): Player = Player(
    id = id,
    firstName = firstName,
    lastName = lastName,
    team = team?.toDomain(),
)

internal inline fun <T, R> ListResponseDto<T>.toPage(transform: (T) -> R): Page<R> = Page(
    items = data.map(transform),
    nextCursor = meta?.nextCursor,
)
