package com.nb.app.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nb.app.core.ui.theme.NbaTheme
import com.nb.app.core.ui.theme.displayFontFamily
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.ic_chevron_right
import org.jetbrains.compose.resources.painterResource

internal val CardShape = RoundedCornerShape(14.dp)
internal val ScreenPadding = 16.dp
internal val CardSpacing = 8.dp

/** White card with a thin border, the base of every list item. Clickable when [onClick] is set. */
@Composable
fun ListCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val color = MaterialTheme.colorScheme.surfaceContainer
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = CardShape, color = color, border = border) {
            content()
        }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = CardShape, color = color, border = border) {
            content()
        }
    }
}

/** A card laid out as one row: leading badge, text in the middle, trailing content. */
@Composable
fun ListCardRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    ListCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** Round badge with a short label (team abbreviation or a player's initials). */
@Composable
fun Badge(
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    muted: Boolean = false,
) {
    val container = if (muted) NbaTheme.colors.mutedContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (muted) NbaTheme.colors.muted else MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Surface(shape = CircleShape, color = container, modifier = Modifier.size(size)) {}
        Text(
            text = label,
            color = content,
            fontFamily = displayFontFamily(),
            fontSize = (size.value * 0.38f).sp,
            maxLines = 1,
        )
    }
}

/** Small rounded label, e.g. the conference or a game result. */
@Composable
fun Pill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(shape = CircleShape, color = containerColor, modifier = modifier) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun ChevronIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_chevron_right),
        contentDescription = null,
        modifier = modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
