package com.nb.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.presentation.home.HomeScreen
import com.nb.app.presentation.navigation.AppTab
import com.nb.app.presentation.navigation.NbaBottomBar
import com.nb.app.presentation.navigation.ScopedViewModelStores
import com.nb.app.presentation.navigation.SelectedTeam
import com.nb.app.presentation.players.PlayersScreen
import com.nb.app.presentation.teamgames.TeamGamesSheet

/**
 * Root of the app: two tabs (Home and Players) and the team games bottom sheet,
 * which both tabs open by setting [SelectedTeam].
 */
@Composable
fun App() {
    NbaTheme {
        var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
        var selectedTeam by rememberSaveable(stateSaver = SelectedTeam.Saver) {
            mutableStateOf<SelectedTeam?>(null)
        }
        // Keeps each tab's saveable UI state (scroll position, search text...) while switching tabs.
        val tabStateHolder = rememberSaveableStateHolder()
        val scopedViewModelStores = viewModel { ScopedViewModelStores() }

        Scaffold(
            bottomBar = {
                NbaBottomBar(currentTab = currentTab, onTabSelected = { currentTab = it })
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                tabStateHolder.SaveableStateProvider(key = currentTab.name) {
                    when (currentTab) {
                        AppTab.HOME -> HomeScreen(onTeamClick = { selectedTeam = it })
                        AppTab.PLAYERS -> PlayersScreen(onPlayerTeamClick = { selectedTeam = it })
                    }
                }
            }
        }

        selectedTeam?.let { team ->
            val storeKey = "team_games_${team.id}"
            val viewModelStoreOwner = remember(storeKey) { scopedViewModelStores.ownerFor(storeKey) }
            TeamGamesSheet(
                team = team,
                viewModelStoreOwner = viewModelStoreOwner,
                onDismiss = {
                    scopedViewModelStores.clear(storeKey)
                    selectedTeam = null
                },
            )
        }
    }
}
