package com.nb.app.presentation.navigation

import androidx.compose.animation.core.animate
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlinx.coroutines.flow.filter

/**
 * Hides the bottom navigation bar while the content scrolls towards the end and brings it back
 * when it scrolls back, or when the list can't scroll any further in either direction.
 */
@Stable
class BottomBarScrollState {
    var heightPx by mutableFloatStateOf(0f)
    var offsetPx by mutableFloatStateOf(0f)
        private set

    val visibleHeightPx: Float get() = (heightPx - offsetPx).coerceAtLeast(0f)

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            onScroll(consumedY = consumed.y, availableY = available.y)
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            settle()
            return Velocity.Zero
        }
    }

    internal fun onScroll(consumedY: Float, availableY: Float) {
        offsetPx = (offsetPx - consumedY - abs(availableY)).coerceIn(0f, heightPx)
    }

    internal fun settleTarget(): Float = if (offsetPx > heightPx / 2f) heightPx else 0f

    suspend fun settle() = animateTo(settleTarget())

    suspend fun show() = animateTo(0f)

    private suspend fun animateTo(target: Float) {
        if (offsetPx == target) return
        animate(initialValue = offsetPx, targetValue = target) { value, _ -> offsetPx = value }
    }
}

val LocalBottomBarScrollState = staticCompositionLocalOf { BottomBarScrollState() }

@Composable
fun rememberBottomBarScrollState(): BottomBarScrollState = remember { BottomBarScrollState() }

/** Shows the bottom bar while in composition, for content that can't be scrolled to reveal it. */
@Composable
fun ShowBottomBarEffect() {
    val state = LocalBottomBarScrollState.current
    LaunchedEffect(state) { state.show() }
}

/** Shows the bottom bar whenever [listState] can't scroll in either direction. */
@Composable
fun ShowBottomBarWhenNotScrollable(listState: LazyListState) {
    val state = LocalBottomBarScrollState.current
    LaunchedEffect(listState, state) {
        snapshotFlow { listState.canScrollForward || listState.canScrollBackward }
            .filter { canScroll -> !canScroll }
            .collect { state.show() }
    }
}

/** Full height of the bottom bar, to keep the end of a list reachable above it. */
@Composable
fun bottomBarHeight(): Dp = with(LocalDensity.current) { LocalBottomBarScrollState.current.heightPx.toDp() }
