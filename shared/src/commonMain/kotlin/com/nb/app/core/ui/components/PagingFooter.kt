package com.nb.app.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import com.nb.app.core.ui.toApiError
import com.nb.app.core.ui.toMessage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Spinner while the next page loads, the error with a retry button, or [endReachedMessage] at the end. */
fun LazyListScope.pagingFooter(
    appendState: LoadState,
    endReachedMessage: StringResource,
    onRetryClick: () -> Unit,
) {
    when (appendState) {
        is LoadState.Loading -> item(key = "paging_loading") { LoadingMoreIndicator() }
        is LoadState.Error -> item(key = "paging_error") {
            ErrorState(
                message = appendState.error.toApiError().toMessage(),
                onRetry = onRetryClick,
                modifier = Modifier.fillMaxWidth(),
                fillMaxSize = false,
            )
        }
        is LoadState.NotLoading -> if (appendState.endOfPaginationReached) {
            item(key = "paging_end") {
                Text(
                    text = stringResource(endReachedMessage),
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
