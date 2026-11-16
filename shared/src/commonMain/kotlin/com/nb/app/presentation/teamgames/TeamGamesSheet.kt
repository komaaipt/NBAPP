package com.nb.app.presentation.teamgames

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nb.app.core.ui.components.Badge
import com.nb.app.core.ui.components.CardSpacing
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.EndlessScrollEffect
import com.nb.app.core.ui.components.ErrorState
import com.nb.app.core.ui.components.ListCard
import com.nb.app.core.ui.components.LoadingState
import com.nb.app.core.ui.components.Pill
import com.nb.app.core.ui.components.ScreenPadding
import com.nb.app.core.ui.components.pagingFooter
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.theme.scoreTextStyle
import com.nb.app.core.ui.toMessage
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Team
import com.nb.app.presentation.navigation.SelectedTeam
import kotlinx.coroutines.launch
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.back_to_home
import nbapp.shared.generated.resources.back_to_home_description
import nbapp.shared.generated.resources.game_loss
import nbapp.shared.generated.resources.game_loss_spoken
import nbapp.shared.generated.resources.game_result_description
import nbapp.shared.generated.resources.game_win
import nbapp.shared.generated.resources.game_win_spoken
import nbapp.shared.generated.resources.games_empty
import nbapp.shared.generated.resources.games_end_reached
import nbapp.shared.generated.resources.ic_chevron_left
import nbapp.shared.generated.resources.season_header
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Bottom sheet listing the games of [team], newest first, with endless scroll. It is shared by
 * the Home and Players tabs and closes with the system Back button, by dragging it down, or
 * with the "< Home" button.
 *
 * @param viewModelStoreOwner scope of the sheet's ViewModel, cleared when the sheet closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamGamesSheet(
    team: SelectedTeam,
    viewModelStoreOwner: ViewModelStoreOwner,
    onDismiss: () -> Unit,
) {
    val viewModel: TeamGamesViewModel = koinViewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(team.id) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        TeamGamesContent(
            team = team,
            uiState = uiState,
            onBackClick = hideSheet,
            onLoadMore = viewModel::onLoadMore,
            onRetryClick = viewModel::onRetryClick,
        )
    }
}

@Composable
fun TeamGamesContent(
    team: SelectedTeam,
    uiState: TeamGamesUiState,
    onBackClick: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TeamGamesTopBar(title = team.fullName, onBackClick = onBackClick)
        val error = uiState.error
        when {
            uiState.games.isNotEmpty() -> GameList(
                teamId = team.id,
                uiState = uiState,
                onLoadMore = onLoadMore,
                onRetryClick = onRetryClick,
            )
            uiState.isLoading -> LoadingState()
            error != null -> ErrorState(message = error.toMessage(), onRetry = onRetryClick)
            else -> EmptyState(message = Res.string.games_empty)
        }
    }
}

/** Games grouped by season, with a header every time the season changes. */
@Composable
private fun GameList(
    teamId: Int,
    uiState: TeamGamesUiState,
    onLoadMore: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    EndlessScrollEffect(listState = listState, onLoadMore = onLoadMore)

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
    ) {
        uiState.games.forEachIndexed { index, game ->
            if (index == 0 || uiState.games[index - 1].season != game.season) {
                item(key = "season_${game.season}") { SeasonHeader(season = game.season) }
            }
            item(key = game.id) { GameCard(game = game, teamId = teamId) }
        }
        pagingFooter(
            isLoadingMore = uiState.isLoadingMore,
            loadMoreError = uiState.loadMoreError,
            endReached = uiState.endReached,
            endReachedMessage = Res.string.games_end_reached,
            onRetryClick = onRetryClick,
        )
    }
}

@Composable
private fun SeasonHeader(season: Int) {
    Text(
        text = stringResource(Res.string.season_header, formatSeason(season)),
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Scoreboard: home team on the left, visitor on the right, scores in the middle. The losing side
 * is muted, and a W/L pill shows the result for the team the sheet was opened for.
 */
@Composable
private fun GameCard(
    game: Game,
    teamId: Int,
) {
    val homeWon = game.homeTeamScore > game.visitorTeamScore
    val visitorWon = game.visitorTeamScore > game.homeTeamScore
    val isWin = game.isWinFor(teamId)
    val resultText = when (isWin) {
        true -> stringResource(Res.string.game_win)
        false -> stringResource(Res.string.game_loss)
        null -> ""
    }
    val spokenResult = when (isWin) {
        true -> stringResource(Res.string.game_win_spoken)
        false -> stringResource(Res.string.game_loss_spoken)
        null -> ""
    }
    val description = stringResource(
        Res.string.game_result_description,
        game.homeTeam.fullName,
        game.homeTeamScore,
        game.visitorTeam.fullName,
        game.visitorTeamScore,
        spokenResult,
    )

    ListCard(modifier = Modifier.clearAndSetSemantics { contentDescription = description }) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${formatGameDate(game.date)} · ${game.status}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isWin != null) ResultPill(isWin = isWin, text = resultText)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamSide(team = game.homeTeam, muted = visitorWon, modifier = Modifier.weight(1f))
                Score(game.homeTeamScore, muted = visitorWon)
                Text(
                    text = "–",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Score(game.visitorTeamScore, muted = homeWon)
                TeamSide(team = game.visitorTeam, muted = homeWon, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TeamSide(
    team: Team,
    muted: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Badge(label = team.abbreviation, size = 36.dp, muted = muted)
        Spacer(Modifier.height(6.dp))
        Text(
            text = team.fullName,
            style = MaterialTheme.typography.labelMedium,
            color = if (muted) NbaTheme.colors.muted else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Score(
    value: Int,
    muted: Boolean,
) {
    Text(
        text = value.toString(),
        style = scoreTextStyle(),
        color = if (muted) NbaTheme.colors.muted else MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ResultPill(isWin: Boolean, text: String) {
    Pill(
        text = text,
        containerColor = if (isWin) NbaTheme.colors.winContainer else NbaTheme.colors.lossContainer,
        contentColor = if (isWin) NbaTheme.colors.onWin else NbaTheme.colors.onLoss,
    )
}

@Composable
private fun TeamGamesTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        TextButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_left),
                contentDescription = stringResource(Res.string.back_to_home_description),
                modifier = Modifier.size(20.dp),
            )
            Text(stringResource(Res.string.back_to_home), style = MaterialTheme.typography.labelLarge)
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

@Preview
@Composable
private fun TeamGamesContentPreview() {
    val hawks = Team(1, "Atlanta Hawks", "Hawks", "Atlanta", "East", "ATL")
    val bulls = Team(5, "Chicago Bulls", "Bulls", "Chicago", "East", "CHI")
    NbaTheme {
        TeamGamesContent(
            team = SelectedTeam(id = hawks.id, fullName = hawks.fullName),
            uiState = TeamGamesUiState(
                games = listOf(
                    Game(1, "2026-11-15", "Final", hawks, 103, bulls, 101, season = 2026),
                    Game(2, "2026-11-12", "Final", bulls, 110, hawks, 99, season = 2026),
                ),
                isLoadingMore = true,
            ),
            onBackClick = {},
            onLoadMore = {},
            onRetryClick = {},
        )
    }
}
