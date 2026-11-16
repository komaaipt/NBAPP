package com.nb.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nb.app.core.ui.isScreenReaderOn
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.presentation.home.HomeScreen
import com.nb.app.presentation.navigation.AppTab
import com.nb.app.presentation.navigation.LocalBottomBarScrollState
import com.nb.app.presentation.navigation.NbaBottomBar
import com.nb.app.presentation.navigation.ScopedViewModelStores
import com.nb.app.presentation.navigation.SelectedTeam
import com.nb.app.presentation.navigation.rememberBottomBarScrollState
import com.nb.app.presentation.players.PlayersScreen
import com.nb.app.presentation.splash.SplashScreen
import com.nb.app.presentation.teamgames.TeamGamesSheet

/**
 * Root of the app: two tabs (Home and Players) and the team games bottom sheet,
 * which both tabs open by setting [SelectedTeam].
 */
@Composable
fun App() {
    NbaTheme {
        // Saveable so the splash doesn't replay after a rotation.
        var isSplashVisible by rememberSaveable { mutableStateOf(true) }
        if (isSplashVisible) {
            SplashScreen(onFinished = { isSplashVisible = false })
            return@NbaTheme
        }

        var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
        var selectedTeam by rememberSaveable(stateSaver = SelectedTeam.Saver) {
            mutableStateOf<SelectedTeam?>(null)
        }
        // Keeps each tab's saveable UI state (scroll position, search text...) while switching tabs.
        val tabStateHolder = rememberSaveableStateHolder()
        val scopedViewModelStores = viewModel { ScopedViewModelStores() }

        val bottomBarState = rememberBottomBarScrollState()
        val isScreenReaderOn = isScreenReaderOn()
        LaunchedEffect(currentTab, isScreenReaderOn) { bottomBarState.show() }

        val backgroundGradient = Brush.verticalGradient(
            listOf(MaterialTheme.colorScheme.background, NbaTheme.colors.backgroundGradientEnd),
        )
        Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .then(if (isScreenReaderOn) Modifier else Modifier.nestedScroll(bottomBarState.nestedScrollConnection)),
            ) {
                CompositionLocalProvider(LocalBottomBarScrollState provides bottomBarState) {
                    tabStateHolder.SaveableStateProvider(key = currentTab.name) {
                        when (currentTab) {
                            AppTab.HOME -> HomeScreen(onTeamClick = { selectedTeam = it })
                            AppTab.PLAYERS -> PlayersScreen(onPlayerTeamClick = { selectedTeam = it })
                        }
                    }
                }
            }
            NbaBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { bottomBarState.heightPx = it.height.toFloat() }
                    .graphicsLayer {
                        translationY = bottomBarState.offsetPx
                        alpha = if (bottomBarState.heightPx > 0f) bottomBarState.visibleHeightPx / bottomBarState.heightPx else 1f
                    },
            )
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
