package com.nb.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nb.app.core.ui.components.Badge
import com.nb.app.core.ui.components.CardSpacing
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.ErrorState
import com.nb.app.core.ui.components.ListCardRow
import com.nb.app.core.ui.components.LoadingState
import com.nb.app.core.ui.components.Pill
import com.nb.app.core.ui.components.ScreenHeader
import com.nb.app.core.ui.components.ScreenPadding
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.toMessage
import com.nb.app.domain.model.Team
import com.nb.app.presentation.navigation.SelectedTeam
import com.nb.app.presentation.navigation.ShowBottomBarEffect
import com.nb.app.presentation.navigation.ShowBottomBarWhenNotScrollable
import com.nb.app.presentation.navigation.bottomBarHeight
import com.nb.app.presentation.navigation.toSelectedTeam
import nbapp.shared.generated.resources.Res
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
        if (uiState.teams.isEmpty()) ShowBottomBarEffect()
        when {
            uiState.teams.isNotEmpty() -> TeamList(teams = uiState.teams, onTeamClick = onTeamClick)
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

/** Filled pill showing the current order; its title changes with the selected option. */
@Composable
private fun SortButton(
    sortOption: TeamSortOption,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_sort),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = stringResource(sortOption.label), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TeamList(
    teams: List<Team>,
    onTeamClick: (Team) -> Unit,
) {
    val listState = rememberLazyListState()
    ShowBottomBarWhenNotScrollable(listState)
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = ScreenPadding + bottomBarHeight()),
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
    ) {
        items(items = teams, key = { it.id }) { team ->
            TeamCard(team = team, onClick = { onTeamClick(team) })
        }
    }
}

/** Abbreviation badge, full name and city, and the conference on the right. */
@Composable
private fun TeamCard(
    team: Team,
    onClick: () -> Unit,
) {
    ListCardRow(onClick = onClick) {
        Badge(label = team.abbreviation)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = team.fullName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = team.city,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Pill(
            text = team.conference,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    NbaTheme {
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
