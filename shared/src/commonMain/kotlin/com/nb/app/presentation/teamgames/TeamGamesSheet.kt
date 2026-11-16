package com.nb.app.presentation.teamgames

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.presentation.navigation.SelectedTeam
import kotlinx.coroutines.launch
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.back_to_home
import nbapp.shared.generated.resources.back_to_home_description
import nbapp.shared.generated.resources.ic_chevron_left
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom sheet listing the games of [team]. It is shared by the Home and Players tabs.
 * It closes with the system Back button, by dragging it down, or with the "< Home" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamGamesSheet(
    team: SelectedTeam,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    val hideSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TeamGamesTopBar(title = team.fullName, onBackClick = hideSheet)
            // The games table with endless scroll comes in Phase 4.
            EmptyState()
        }
    }
}

@Composable
private fun TeamGamesTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        TextButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_left),
                contentDescription = stringResource(Res.string.back_to_home_description),
                modifier = Modifier.size(20.dp),
            )
            Text(stringResource(Res.string.back_to_home))
        }
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 96.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
