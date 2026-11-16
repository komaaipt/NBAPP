package com.nb.app.presentation.players

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.ScreenHeader
import com.nb.app.presentation.navigation.SelectedTeam
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.players_title
import org.jetbrains.compose.resources.stringResource

/** Placeholder; player search and its ViewModel come in Phase 5. */
@Composable
fun PlayersScreen(
    onPlayerTeamClick: (SelectedTeam) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(Res.string.players_title))
        EmptyState()
    }
}
