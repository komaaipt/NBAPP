package com.nb.app.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nb.app.core.network.ApiError
import com.nb.app.core.ui.toMessage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Last item of a paginated list: a spinner while the next page loads, the error with a retry
 * button when it failed, or [endReachedMessage] once there is nothing more to load.
 */
fun LazyListScope.pagingFooter(
    isLoadingMore: Boolean,
    loadMoreError: ApiError?,
    endReached: Boolean,
    endReachedMessage: StringResource,
    onRetryClick: () -> Unit,
) {
    when {
        isLoadingMore -> item(key = "paging_loading") { LoadingMoreIndicator() }
        loadMoreError != null -> item(key = "paging_error") {
            ErrorState(
                message = loadMoreError.toMessage(),
                onRetry = onRetryClick,
                modifier = Modifier.fillMaxWidth(),
                fillMaxSize = false,
            )
        }
        endReached -> item(key = "paging_end") {
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
