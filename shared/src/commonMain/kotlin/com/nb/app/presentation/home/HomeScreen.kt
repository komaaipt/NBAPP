package com.nb.app.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.ScreenHeader
import com.nb.app.presentation.navigation.SelectedTeam
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource

/** Placeholder; the team list, sorting dialog and ViewModel come in Phase 3. */
@Composable
fun HomeScreen(
    onTeamClick: (SelectedTeam) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(Res.string.home_title))
        EmptyState()
    }
}
