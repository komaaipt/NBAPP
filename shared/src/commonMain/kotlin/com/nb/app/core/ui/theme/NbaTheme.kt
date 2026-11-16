package com.nb.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NbaBlue,
    onPrimary = Color.White,
    secondary = NbaRed,
    onSecondary = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = NbaBlueLight,
    onPrimary = NbaNavy,
    secondary = NbaRedLight,
    onSecondary = NbaNavy,
)

@Composable
fun NbaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
