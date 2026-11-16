package com.nb.app.data.remote

import com.nb.app.data.remote.dto.GameDto
import com.nb.app.data.remote.dto.ListResponseDto
import com.nb.app.data.remote.dto.PlayerDto
import com.nb.app.data.remote.dto.TeamDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Thin wrapper around the balldontlie REST endpoints. It only knows about HTTP and DTOs;
 * error mapping and DTO-to-domain conversion live in the repositories.
 */
internal class NbaRemoteDataSource(private val client: HttpClient) {

    suspend fun getTeams(): ListResponseDto<TeamDto> =
        client.get("teams").body()

    suspend fun getGames(
        teamId: Int,
        season: Int,
        cursor: Int?,
        perPage: Int,
    ): ListResponseDto<GameDto> =
        client.get("games") {
            parameter("team_ids[]", teamId)
            parameter("seasons[]", season)
            parameter("per_page", perPage)
            cursor?.let { parameter("cursor", it) }
        }.body()

    suspend fun searchPlayers(query: String, cursor: Int?, perPage: Int): ListResponseDto<PlayerDto> =
        client.get("players") {
            parameter("search", query)
            parameter("per_page", perPage)
            cursor?.let { parameter("cursor", it) }
        }.body()
}
