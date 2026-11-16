package com.nb.app.presentation.teamgames

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nb.app.core.ui.components.DataTable
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.EndlessScrollEffect
import com.nb.app.core.ui.components.ErrorState
import com.nb.app.core.ui.components.LoadingMoreIndicator
import com.nb.app.core.ui.components.LoadingState
import com.nb.app.core.ui.components.TableColumn
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.toMessage
import com.nb.app.domain.model.Game
import com.nb.app.domain.model.Team
import com.nb.app.presentation.navigation.SelectedTeam
import kotlinx.coroutines.launch
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.back_to_home
import nbapp.shared.generated.resources.back_to_home_description
import nbapp.shared.generated.resources.column_home_name
import nbapp.shared.generated.resources.column_home_score
import nbapp.shared.generated.resources.column_visitor_name
import nbapp.shared.generated.resources.column_visitor_score
import nbapp.shared.generated.resources.games_empty
import nbapp.shared.generated.resources.games_end_reached
import nbapp.shared.generated.resources.ic_chevron_left
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
        // Same color as the tabs, so the sticky table header blends in.
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TeamGamesContent(
            title = team.fullName,
            uiState = uiState,
            onBackClick = hideSheet,
            onLoadMore = viewModel::onLoadMore,
            onRetryClick = viewModel::onRetryClick,
        )
    }
}

@Composable
fun TeamGamesContent(
    title: String,
    uiState: TeamGamesUiState,
    onBackClick: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TeamGamesTopBar(title = title, onBackClick = onBackClick)
        val error = uiState.error
        when {
            uiState.games.isNotEmpty() -> GameTable(
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

@Composable
private fun GameTable(
    uiState: TeamGamesUiState,
    onLoadMore: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    EndlessScrollEffect(listState = listState, onLoadMore = onLoadMore)

    val columns = listOf(
        TableColumn(title = stringResource(Res.string.column_home_name), weight = 1.4f),
        TableColumn(
            title = stringResource(Res.string.column_home_score),
            weight = 0.8f,
            textAlign = TextAlign.Center,
        ),
        TableColumn(title = stringResource(Res.string.column_visitor_name), weight = 1.4f),
        TableColumn(
            title = stringResource(Res.string.column_visitor_score),
            weight = 0.8f,
            textAlign = TextAlign.Center,
        ),
    )
    val loadMoreError = uiState.loadMoreError
    DataTable(
        columns = columns,
        items = uiState.games,
        key = { it.id },
        cells = { game ->
            listOf(
                game.homeTeam.fullName,
                game.homeTeamScore.toString(),
                game.visitorTeam.fullName,
                game.visitorTeamScore.toString(),
            )
        },
        state = listState,
        footer = {
            when {
                uiState.isLoadingMore -> item(key = "loading_more") { LoadingMoreIndicator() }
                loadMoreError != null -> item(key = "load_more_error") {
                    ErrorState(
                        message = loadMoreError.toMessage(),
                        onRetry = onRetryClick,
                        modifier = Modifier.fillMaxWidth(),
                        fillMaxSize = false,
                    )
                }
                uiState.endReached -> item(key = "end_reached") {
                    Text(
                        text = stringResource(Res.string.games_end_reached),
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
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

@Preview
@Composable
private fun TeamGamesContentPreview() {
    val hawks = Team(1, "Atlanta Hawks", "Hawks", "Atlanta", "East", "ATL")
    val bulls = Team(5, "Chicago Bulls", "Bulls", "Chicago", "East", "CHI")
    NbaTheme {
        Surface {
            TeamGamesContent(
                title = hawks.fullName,
                uiState = TeamGamesUiState(
                    games = listOf(
                        Game(1, "2026-11-15", "Final", hawks, 103, bulls, 101),
                        Game(2, "2026-11-12", "Final", bulls, 99, hawks, 110),
                    ),
                    isLoadingMore = true,
                ),
                onBackClick = {},
                onLoadMore = {},
                onRetryClick = {},
            )
        }
    }
}
