package com.nb.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.clear_search_description
import nbapp.shared.generated.resources.close_search_description
import nbapp.shared.generated.resources.ic_close
import nbapp.shared.generated.resources.ic_search
import nbapp.shared.generated.resources.open_search_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val FabSize = 56.dp
private val FabCornerRadius = 16.dp

/**
 * Search FAB that grows into a full-width search field in the same spot. Place it at the bottom
 * of an `imePadding()` container so the field sits right above the keyboard.
 */
@Composable
fun ExpandingSearchFab(
    expanded: Boolean,
    query: String,
    placeholder: String,
    onExpandedChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val collapse = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onQueryChange("")
        onExpandedChange(false)
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = expanded,
        onBackCompleted = collapse,
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            withFrameNanos { }
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val colors = MaterialTheme.colorScheme
    val morphSpec = spring<Dp>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
    val cornerRadius by animateDpAsState(if (expanded) FabSize / 2 else FabCornerRadius, morphSpec)
    val containerColor by animateColorAsState(if (expanded) colors.surfaceContainer else colors.primary)
    val iconColor by animateColorAsState(if (expanded) colors.onSurfaceVariant else colors.onPrimary)
    val fieldAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = if (expanded) 200 else 90, delayMillis = if (expanded) 120 else 0),
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width by animateDpAsState(if (expanded) maxWidth else FabSize, morphSpec)
        val shape = RoundedCornerShape(cornerRadius)
        val openDescription = stringResource(Res.string.open_search_description)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(width)
                .height(FabSize)
                .clip(shape)
                .then(
                    if (expanded) {
                        Modifier
                    } else {
                        Modifier.clickable(onClickLabel = openDescription, role = Role.Button) {
                            onExpandedChange(true)
                        }
                    },
                ),
            shape = shape,
            color = containerColor,
            border = if (expanded) BorderStroke(1.5.dp, colors.primary) else null,
            shadowElevation = 6.dp,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(FabSize), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = if (expanded) null else openDescription,
                        tint = iconColor,
                    )
                }
                if (expanded || fieldAlpha > 0f) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(fieldAlpha)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                        cursorBrush = SolidColor(colors.primary),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search,
                        ),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                    IconButton(
                        onClick = { if (query.isNotEmpty()) onQueryChange("") else collapse() },
                        modifier = Modifier.alpha(fieldAlpha),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = stringResource(
                                if (query.isNotEmpty()) Res.string.clear_search_description else Res.string.close_search_description,
                            ),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
