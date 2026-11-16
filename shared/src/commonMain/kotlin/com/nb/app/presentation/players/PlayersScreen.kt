package com.nb.app.presentation.players

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nb.app.core.ui.components.DataTable
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.EndlessScrollEffect
import com.nb.app.core.ui.components.ErrorState
import com.nb.app.core.ui.components.LoadingState
import com.nb.app.core.ui.components.ScreenHeader
import com.nb.app.core.ui.components.TableColumn
import com.nb.app.core.ui.components.pagingFooter
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.toMessage
import com.nb.app.domain.model.Player
import com.nb.app.domain.model.Team
import com.nb.app.presentation.navigation.SelectedTeam
import com.nb.app.presentation.navigation.toSelectedTeam
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.clear_search_description
import nbapp.shared.generated.resources.column_first_name
import nbapp.shared.generated.resources.column_last_name
import nbapp.shared.generated.resources.column_team
import nbapp.shared.generated.resources.ic_close
import nbapp.shared.generated.resources.ic_search
import nbapp.shared.generated.resources.players_empty
import nbapp.shared.generated.resources.players_end_reached
import nbapp.shared.generated.resources.players_prompt
import nbapp.shared.generated.resources.players_title
import nbapp.shared.generated.resources.search_players_hint
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Players tab: search players by name; tapping one opens their team's games. */
@Composable
fun PlayersScreen(
    onPlayerTeamClick: (SelectedTeam) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayersViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlayersContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onClearQueryClick = viewModel::onClearQueryClick,
        onPlayerClick = { player -> player.team?.let { onPlayerTeamClick(it.toSelectedTeam()) } },
        onLoadMore = viewModel::onLoadMore,
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Composable
fun PlayersContent(
    uiState: PlayersUiState,
    onQueryChange: (String) -> Unit,
    onClearQueryClick: () -> Unit,
    onPlayerClick: (Player) -> Unit,
    onLoadMore: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(Res.string.players_title))
        SearchField(
            query = uiState.query,
            onQueryChange = onQueryChange,
            onClearClick = onClearQueryClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        val error = uiState.error
        when {
            uiState.players.isNotEmpty() -> PlayerTable(
                uiState = uiState,
                onPlayerClick = onPlayerClick,
                onLoadMore = onLoadMore,
                onRetryClick = onRetryClick,
            )
            uiState.isLoading -> LoadingState()
            error != null -> ErrorState(message = error.toMessage(), onRetry = onRetryClick)
            uiState.hasSearched -> EmptyState(
                message = Res.string.players_empty,
                formatArgs = listOf(uiState.query.trim()),
            )
            else -> EmptyState(message = Res.string.players_prompt)
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(Res.string.search_players_hint)) },
        leadingIcon = {
            Icon(painter = painterResource(Res.drawable.ic_search), contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = stringResource(Res.string.clear_search_description),
                    )
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
    )
}

@Composable
private fun PlayerTable(
    uiState: PlayersUiState,
    onPlayerClick: (Player) -> Unit,
    onLoadMore: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    EndlessScrollEffect(listState = listState, onLoadMore = onLoadMore)

    val columns = listOf(
        TableColumn(title = stringResource(Res.string.column_first_name)),
        TableColumn(title = stringResource(Res.string.column_last_name)),
        TableColumn(title = stringResource(Res.string.column_team), weight = 1.6f),
    )
    DataTable(
        columns = columns,
        items = uiState.players,
        key = { it.id },
        cells = { player -> listOf(player.firstName, player.lastName, player.team?.fullName.orEmpty()) },
        onItemClick = onPlayerClick,
        // Historical players may have no team, so there are no games to show.
        isItemClickable = { it.team != null },
        state = listState,
        footer = {
            pagingFooter(
                isLoadingMore = uiState.isLoadingMore,
                loadMoreError = uiState.loadMoreError,
                endReached = uiState.endReached,
                endReachedMessage = Res.string.players_end_reached,
                onRetryClick = onRetryClick,
            )
        },
    )
}

@Preview
@Composable
private fun PlayersContentPreview() {
    val cavaliers = Team(6, "Cleveland Cavaliers", "Cavaliers", "Cleveland", "East", "CLE")
    NbaTheme {
        Surface {
            PlayersContent(
                uiState = PlayersUiState(
                    query = "Ilgauskas",
                    players = listOf(Player(1, "Žydrūnas", "Ilgauskas", cavaliers)),
                    hasSearched = true,
                    endReached = true,
                ),
                onQueryChange = {},
                onClearQueryClick = {},
                onPlayerClick = {},
                onLoadMore = {},
                onRetryClick = {},
            )
        }
    }
}
