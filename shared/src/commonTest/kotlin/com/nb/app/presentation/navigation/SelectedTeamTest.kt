package com.nb.app.presentation.navigation

import androidx.compose.runtime.saveable.SaverScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelectedTeamTest {

    private val saverScope = SaverScope { true }

    @Test
    fun saver_restoresSelectedTeam() {
        val team = SelectedTeam(id = 1, fullName = "Atlanta Hawks")

        val saved = with(SelectedTeam.Saver) { saverScope.save(team) }!!

        assertEquals(team, SelectedTeam.Saver.restore(saved))
    }

    @Test
    fun saver_savesNothingWhenNoTeamIsSelected() {
        val saved = with(SelectedTeam.Saver) { saverScope.save(null) }

        assertNull(saved)
    }
}
