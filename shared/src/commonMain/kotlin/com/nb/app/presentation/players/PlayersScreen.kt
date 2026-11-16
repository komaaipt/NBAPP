package com.nb.app.presentation.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.nb.app.core.ui.components.Badge
import com.nb.app.core.ui.components.CardSpacing
import com.nb.app.core.ui.components.ChevronIcon
import com.nb.app.core.ui.components.ListCardRow
import com.nb.app.core.ui.components.ScreenPadding
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.ErrorState
import com.nb.app.core.ui.components.LoadingState
import com.nb.app.core.ui.components.ScreenHeader
import com.nb.app.core.ui.components.pagingFooter
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.toApiError
import com.nb.app.core.ui.toMessage
import com.nb.app.domain.model.Player
import com.nb.app.domain.model.Team
import com.nb.app.presentation.navigation.SelectedTeam
import com.nb.app.presentation.navigation.toSelectedTeam
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.clear_search_description
import nbapp.shared.generated.resources.ic_close
import nbapp.shared.generated.resources.ic_search
import nbapp.shared.generated.resources.players_empty
import nbapp.shared.generated.resources.players_end_reached
import nbapp.shared.generated.resources.players_prompt
import nbapp.shared.generated.resources.players_title
import nbapp.shared.generated.resources.search_players_hint
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.viewmodel.koinViewModel

/** Players tab: search players by name; tapping one opens their team's games. */
@Composable
fun PlayersScreen(
    onPlayerTeamClick: (SelectedTeam) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayersViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchedQuery by viewModel.searchedQuery.collectAsStateWithLifecycle()
    val players = viewModel.players.collectAsLazyPagingItems()
    PlayersContent(
        query = query,
        searchedQuery = searchedQuery,
        players = players,
        onQueryChange = viewModel::onQueryChange,
        onClearQueryClick = viewModel::onClearQueryClick,
        onPlayerClick = { player -> player.team?.let { onPlayerTeamClick(it.toSelectedTeam()) } },
        modifier = modifier,
    )
}

@Composable
fun PlayersContent(
    query: String,
    searchedQuery: String,
    players: LazyPagingItems<Player>,
    onQueryChange: (String) -> Unit,
    onClearQueryClick: () -> Unit,
    onPlayerClick: (Player) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(Res.string.players_title))
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onClearClick = onClearQueryClick,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        )
        val refresh = players.loadState.refresh
        when {
            searchedQuery.length < MIN_QUERY_LENGTH -> EmptyState(message = Res.string.players_prompt)
            players.itemCount > 0 -> PlayerList(players = players, onPlayerClick = onPlayerClick)
            refresh is LoadState.Loading -> LoadingState()
            refresh is LoadState.Error -> ErrorState(
                message = refresh.error.toApiError().toMessage(),
                onRetry = players::retry,
            )
            else -> EmptyState(message = Res.string.players_empty, formatArgs = listOf(searchedQuery))
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
    )
}

@Composable
private fun PlayerList(
    players: LazyPagingItems<Player>,
    onPlayerClick: (Player) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
    ) {
        items(count = players.itemCount, key = players.itemKey { it.id }) { index ->
            val player = players[index] ?: return@items
            PlayerCard(player = player, onClick = { onPlayerClick(player) })
        }
        pagingFooter(
            appendState = players.loadState.append,
            endReachedMessage = Res.string.players_end_reached,
            onRetryClick = players::retry,
        )
    }
}

/** Initials, full name and team. Historical players without a team aren't clickable. */
@Composable
private fun PlayerCard(
    player: Player,
    onClick: () -> Unit,
) {
    val team = player.team
    ListCardRow(onClick = if (team != null) onClick else null) {
        Badge(label = player.initials())
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = "${player.firstName} ${player.lastName}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (team != null) {
                Text(
                    text = team.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (team != null) ChevronIcon()
    }
}

private fun Player.initials(): String =
    listOf(firstName, lastName).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

@Preview
@Composable
private fun PlayersContentPreview() {
    val cavaliers = Team(6, "Cleveland Cavaliers", "Cavaliers", "Cleveland", "East", "CLE")
    val players = listOf(Player(1, "Žydrūnas", "Ilgauskas", cavaliers))
    NbaTheme {
        Surface {
            PlayersContent(
                query = "Ilgauskas",
                searchedQuery = "Ilgauskas",
                players = flowOf(PagingData.from(players)).collectAsLazyPagingItems(),
                onQueryChange = {},
                onClearQueryClick = {},
                onPlayerClick = {},
            )
        }
    }
}
