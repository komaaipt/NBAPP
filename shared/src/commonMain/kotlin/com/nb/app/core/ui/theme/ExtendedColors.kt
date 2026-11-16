package com.nb.app.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colors the Material scheme has no role for. Read them through [NbaTheme.colors]. */
@Immutable
data class ExtendedColors(
    /** De-emphasized content, e.g. the losing team on a scoreboard. */
    val muted: Color,
    val mutedContainer: Color,
    val winContainer: Color,
    val onWin: Color,
    val lossContainer: Color,
    val onLoss: Color,
)

internal val LightExtendedColors = ExtendedColors(
    muted = LightMuted,
    mutedContainer = LightMutedContainer,
    winContainer = LightWinContainer,
    onWin = LightOnWin,
    lossContainer = LightLossContainer,
    onLoss = LightOnLoss,
)

internal val DarkExtendedColors = ExtendedColors(
    muted = DarkMuted,
    mutedContainer = DarkMutedContainer,
    winContainer = DarkWinContainer,
    onWin = DarkOnWin,
    lossContainer = DarkLossContainer,
    onLoss = DarkOnLoss,
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
