package com.nb.app.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NbaBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.selectableGroup().padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppTab.entries.forEach { tab ->
                    FloatingNavItem(
                        tab = tab,
                        selected = tab == currentTab,
                        onClick = { onTabSelected(tab) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val indicatorColor by animateColorAsState(if (selected) colors.secondaryContainer else Color.Transparent)
    val contentColor by animateColorAsState(if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant)
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .background(indicatorColor)
            .widthIn(min = 88.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(tab.icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )
        Text(
            text = stringResource(tab.label),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}
