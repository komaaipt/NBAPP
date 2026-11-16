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
            }
          ]
        }
    """.trimIndent()

    fun gamesJson(nextCursor: Int?): String = """
        {
          "data": [
            {
              "id": 15907925, "date": "2025-01-05", "season": 2024, "status": "Final",
              "period": 4, "time": "Final", "postseason": false, "postponed": false,
              "home_team_score": 115, "visitor_team_score": 105,
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
          ],
          "meta": { "next_cursor": ${nextCursor ?: "null"}, "per_page": 25 }
        }
    """.trimIndent()

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
