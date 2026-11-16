package com.nb.app.testing

import com.nb.app.domain.model.Team

/**
 * Teams chosen so that sorting by name, city and conference gives three different orders.
 * [zephyrs] is fictional: its name sorts last but its city sorts first.
 */
object TestData {
    val hawks = Team(1, "Atlanta Hawks", "Hawks", "Atlanta", "East", "ATL")
    val celtics = Team(2, "Boston Celtics", "Celtics", "Boston", "East", "BOS")
    val mavericks = Team(7, "Dallas Mavericks", "Mavericks", "Dallas", "West", "DAL")
    val warriors = Team(10, "Golden State Warriors", "Warriors", "Golden State", "West", "GSW")
    val clippers = Team(13, "LA Clippers", "Clippers", "Los Angeles", "West", "LAC")
    val lakers = Team(14, "Los Angeles Lakers", "Lakers", "Los Angeles", "West", "LAL")
    val heat = Team(16, "Miami Heat", "Heat", "Miami", "East", "MIA")
    val zephyrs = Team(99, "Zephyrs", "Zephyrs", "Albany", "East", "ZEP")
}
