package com.nb.app.core.ui.components

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Calls [onLoadMore] when the last visible item is within [buffer] items of the end of the list.
 *
 * It fires again whenever the item count changes while the user is still near the end, so a
 * short page that doesn't fill the screen keeps loading the next one. The caller is expected to
 * ignore calls while a page is already loading.
 */
@Composable
fun EndlessScrollEffect(
    listState: LazyListState,
    onLoadMore: () -> Unit,
    buffer: Int = 5,
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState, buffer) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            layoutInfo.isNearEnd(buffer) to layoutInfo.totalItemsCount
        }
            .distinctUntilChanged()
            .filter { (isNearEnd, _) -> isNearEnd }
            .collect { currentOnLoadMore() }
    }
}

private fun LazyListLayoutInfo.isNearEnd(buffer: Int): Boolean {
    val lastVisibleIndex = visibleItemsInfo.lastOrNull()?.index ?: return false
    return totalItemsCount > 0 && lastVisibleIndex >= totalItemsCount - 1 - buffer
}
