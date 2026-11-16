package com.nb.app.presentation.navigation

import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.ic_home
import nbapp.shared.generated.resources.ic_players
import nbapp.shared.generated.resources.tab_home
import nbapp.shared.generated.resources.tab_players
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class AppTab(
    val label: StringResource,
    val icon: DrawableResource,
) {
    HOME(label = Res.string.tab_home, icon = Res.drawable.ic_home),
    PLAYERS(label = Res.string.tab_players, icon = Res.drawable.ic_players),
}
