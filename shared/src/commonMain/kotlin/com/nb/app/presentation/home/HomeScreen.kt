package com.nb.app.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nb.app.core.ui.components.DataTable
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.ErrorState
import com.nb.app.core.ui.components.LoadingState
import com.nb.app.core.ui.components.ScreenHeader
import com.nb.app.core.ui.components.TableColumn
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.toMessage
import com.nb.app.domain.model.Team
import com.nb.app.presentation.navigation.SelectedTeam
import com.nb.app.presentation.navigation.toSelectedTeam
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.column_city
import nbapp.shared.generated.resources.column_conference
import nbapp.shared.generated.resources.column_name
import nbapp.shared.generated.resources.home_title
import nbapp.shared.generated.resources.ic_sort
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Home tab: every NBA team, sortable by name, city or conference. */
@Composable
fun HomeScreen(
    onTeamClick: (SelectedTeam) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onTeamClick = { team -> onTeamClick(team.toSelectedTeam()) },
        onSortClick = viewModel::onSortClick,
        onSortOptionSelected = viewModel::onSortOptionSelected,
        onSortDialogDismiss = viewModel::onSortDialogDismiss,
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onTeamClick: (Team) -> Unit,
    onSortClick: () -> Unit,
    onSortOptionSelected: (TeamSortOption) -> Unit,
    onSortDialogDismiss: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(Res.string.home_title)) {
            SortButton(sortOption = uiState.sortOption, onClick = onSortClick)
        }
        val error = uiState.error
        when {
            uiState.teams.isNotEmpty() -> TeamTable(teams = uiState.teams, onTeamClick = onTeamClick)
            uiState.isLoading -> LoadingState()
            error != null -> ErrorState(message = error.toMessage(), onRetry = onRetryClick)
            else -> EmptyState()
        }
    }

    if (uiState.isSortDialogVisible) {
        SortDialog(
            selectedOption = uiState.sortOption,
            onOptionSelected = onSortOptionSelected,
            onDismiss = onSortDialogDismiss,
        )
    }
}

@Composable
private fun SortButton(
    sortOption: TeamSortOption,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_sort),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Text(text = stringResource(sortOption.label))
    }
}

@Composable
private fun TeamTable(
    teams: List<Team>,
    onTeamClick: (Team) -> Unit,
) {
    val columns = listOf(
        TableColumn(title = stringResource(Res.string.column_name), weight = 1.5f),
        TableColumn(title = stringResource(Res.string.column_city), weight = 1.2f),
        TableColumn(title = stringResource(Res.string.column_conference), textAlign = TextAlign.End),
    )
    DataTable(
        columns = columns,
        items = teams,
        key = { it.id },
        cells = { team -> listOf(team.fullName, team.city, team.conference) },
        onItemClick = onTeamClick,
    )
}

@Preview
@Composable
private fun HomeContentPreview() {
    NbaTheme {
        Surface {
            HomeContent(
                uiState = HomeUiState(
                    teams = listOf(
                        Team(1, "Atlanta Hawks", "Hawks", "Atlanta", "East", "ATL"),
                        Team(2, "Boston Celtics", "Celtics", "Boston", "East", "BOS"),
                        Team(10, "Golden State Warriors", "Warriors", "Golden State", "West", "GSW"),
                    ),
                ),
                onTeamClick = {},
                onSortClick = {},
                onSortOptionSelected = {},
                onSortDialogDismiss = {},
                onRetryClick = {},
            )
        }
    }
}
