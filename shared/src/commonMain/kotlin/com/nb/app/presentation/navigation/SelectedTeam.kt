package com.nb.app.presentation.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.nb.app.domain.model.Team

/**
 * The team whose games are shown in the bottom sheet. Only the id is needed to load the games;
 * the name is kept so the sheet title can be shown right away.
 */
@Immutable
data class SelectedTeam(
    val id: Int,
    val fullName: String,
) {
    companion object {
        /**
         * Lets the selection survive configuration changes and process death.
         * A `null` selection saves nothing, so it is restored as the initial `null` value.
         */
        val Saver: Saver<SelectedTeam?, Any> = listSaver(
            save = { team -> if (team == null) emptyList() else listOf(team.id, team.fullName) },
            restore = { values ->
                if (values.isEmpty()) null else SelectedTeam(values[0] as Int, values[1] as String)
            },
        )
    }
}

fun Team.toSelectedTeam(): SelectedTeam = SelectedTeam(id = id, fullName = fullName)
