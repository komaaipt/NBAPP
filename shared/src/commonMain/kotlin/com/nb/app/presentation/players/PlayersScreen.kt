package com.nb.app.presentation.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
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
import com.nb.app.core.ui.components.EmptyState
import com.nb.app.core.ui.components.ErrorState
import com.nb.app.core.ui.components.ExpandingSearchFab
import com.nb.app.core.ui.components.ListCardRow
import com.nb.app.core.ui.components.LoadingState
import com.nb.app.core.ui.components.ScreenHeader
import com.nb.app.core.ui.components.ScreenPadding
import com.nb.app.core.ui.components.pagingFooter
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.toApiError
import com.nb.app.core.ui.toMessage
import com.nb.app.domain.model.Player
import com.nb.app.domain.model.Team
import com.nb.app.presentation.navigation.LocalBottomBarScrollState
import com.nb.app.presentation.navigation.SelectedTeam
import com.nb.app.presentation.navigation.ShowBottomBarEffect
import com.nb.app.presentation.navigation.ShowBottomBarWhenNotScrollable
import com.nb.app.presentation.navigation.bottomBarHeight
import com.nb.app.presentation.navigation.toSelectedTeam
import kotlinx.coroutines.flow.flowOf
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.players_empty
import nbapp.shared.generated.resources.players_end_reached
import nbapp.shared.generated.resources.players_prompt
import nbapp.shared.generated.resources.players_prompt_tap_search
import nbapp.shared.generated.resources.players_title
import nbapp.shared.generated.resources.search_players_hint
import org.jetbrains.compose.resources.stringResource
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
    var isSearchExpanded by rememberSaveable { mutableStateOf(query.isNotEmpty()) }
    PlayersContent(
        query = query,
        searchedQuery = searchedQuery,
        players = players,
        isSearchExpanded = isSearchExpanded,
        onSearchExpandedChange = { isSearchExpanded = it },
        onQueryChange = viewModel::onQueryChange,
        onPlayerClick = { player -> player.team?.let { onPlayerTeamClick(it.toSelectedTeam()) } },
        modifier = modifier,
    )
}

@Composable
fun PlayersContent(
    query: String,
    searchedQuery: String,
    players: LazyPagingItems<Player>,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onPlayerClick: (Player) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            ScreenHeader(title = stringResource(Res.string.players_title))
            val refresh = players.loadState.refresh
            val showsList = (isSearchExpanded || query.isNotEmpty()) &&
                searchedQuery.length >= MIN_QUERY_LENGTH &&
                players.itemCount > 0
            if (!showsList) ShowBottomBarEffect()
            when {
                !isSearchExpanded && query.isEmpty() -> EmptyState(message = Res.string.players_prompt_tap_search)
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
        SearchFabAboveBottomBar(
            expanded = isSearchExpanded,
            query = query,
            onExpandedChange = onSearchExpandedChange,
            onQueryChange = onQueryChange,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SearchFabAboveBottomBar(
    expanded: Boolean,
    query: String,
    onExpandedChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleBarHeight = with(LocalDensity.current) { LocalBottomBarScrollState.current.visibleHeightPx.toDp() }
    val bottomInsets = WindowInsets.ime
        .union(WindowInsets.navigationBars)
        .union(WindowInsets(bottom = visibleBarHeight))
    ExpandingSearchFab(
        expanded = expanded,
        query = query,
        placeholder = stringResource(Res.string.search_players_hint),
        onExpandedChange = onExpandedChange,
        onQueryChange = onQueryChange,
        modifier = modifier.windowInsetsPadding(bottomInsets).padding(ScreenPadding),
    )
}

@Composable
private fun PlayerList(
    players: LazyPagingItems<Player>,
    onPlayerClick: (Player) -> Unit,
) {
    val listState = rememberLazyListState()
    ShowBottomBarWhenNotScrollable(listState)
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = SearchFabClearance + bottomBarHeight()),
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

private val SearchFabClearance = 88.dp

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
                isSearchExpanded = true,
                onSearchExpandedChange = {},
                onQueryChange = {},
                onPlayerClick = {},
            )
        }
    }
}
