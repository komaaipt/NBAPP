package com.nb.app.data

internal object Fixtures {

    val TEAMS_JSON = """
        {
          "data": [
            {
              "id": 1, "conference": "East", "division": "Southeast", "city": "Atlanta",
              "name": "Hawks", "full_name": "Atlanta Hawks", "abbreviation": "ATL"
            },
            {
              "id": 2, "conference": "East", "division": "Atlantic", "city": "Boston",
              "name": "Celtics", "full_name": "Boston Celtics", "abbreviation": "BOS"
            },
            {
              "id": 37, "conference": " ", "division": " ", "city": "Chicago",
              "name": "Stags", "full_name": "Chicago Stags", "abbreviation": "CHS"
            }
          ]
        }
    """.trimIndent()

    /** One game row of the `/games` endpoint. */
    data class GameRow(
        val id: Int,
        val date: String,
        val statusState: String = "final",
        val homeScore: Int = 115,
        val visitorScore: Int = 105,
    )

    fun gamesJson(games: List<GameRow>, nextCursor: Int? = null): String {
        val rows = games.joinToString(",\n") { game ->
            """
            {
              "id": ${game.id}, "date": "${game.date}", "season": 2026,
              "status": "${if (game.statusState == "final") "Final" else game.date + "T00:00:00Z"}",
              "status_state": "${game.statusState}",
              "period": 4, "time": "Final", "postseason": false, "postponed": false,
              "home_team_score": ${game.homeScore}, "visitor_team_score": ${game.visitorScore},
              "home_q1": 29, "home_ot1": null,
              "home_team": {
                "id": 6, "conference": "East", "division": "Central", "city": "Cleveland",
                "name": "Cavaliers", "full_name": "Cleveland Cavaliers", "abbreviation": "CLE"
              },
              "visitor_team": {
                "id": 4, "conference": "East", "division": "Southeast", "city": "Charlotte",
                "name": "Hornets", "full_name": "Charlotte Hornets", "abbreviation": "CHA"
              }
            }
            """.trimIndent()
        }
        val meta = if (nextCursor == null) """{ "per_page": 100 }""" else """{ "next_cursor": $nextCursor, "per_page": 100 }"""
        return """{ "data": [ $rows ], "meta": $meta }"""
    }

    val PLAYERS_JSON = """
        {
          "data": [
            {
              "id": 115, "first_name": "Stephen", "last_name": "Curry", "position": "G",
              "height": "6-2", "weight": "185", "jersey_number": "30",
              "team": {
                "id": 10, "conference": "West", "division": "Pacific", "city": "Golden State",
                "name": "Warriors", "full_name": "Golden State Warriors", "abbreviation": "GSW"
              }
            },
            {
              "id": 999, "first_name": "Old", "last_name": "Timer", "position": "",
              "team": null
            }
          ],
          "meta": { "next_cursor": 25, "per_page": 25 }
        }
    """.trimIndent()
}
