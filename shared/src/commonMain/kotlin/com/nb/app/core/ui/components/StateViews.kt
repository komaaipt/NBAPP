package com.nb.app.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nb.app.core.ui.theme.NbaTheme
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.empty_list
import nbapp.shared.generated.resources.error_network
import nbapp.shared.generated.resources.loading_description
import nbapp.shared.generated.resources.retry
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Full-area loading spinner, used while the first page of a list loads. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.loading_description)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

/** Small spinner shown at the end of a list while the next page loads. */
@Composable
fun LoadingMoreIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.loading_description)
    Box(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp).semantics { contentDescription = description },
            strokeWidth = 2.dp,
        )
    }
}

/** Error message with an optional retry button. */
@Composable
fun ErrorState(
    message: StringResource,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    MessageState(text = stringResource(message), modifier = modifier) {
        if (onRetry != null) {
            Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
        }
    }
}

/** Shown when a request succeeds but there is nothing to list. */
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    message: StringResource = Res.string.empty_list,
) {
    MessageState(text = stringResource(message), modifier = modifier)
}

@Composable
private fun MessageState(
    text: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action()
    }
}

@Preview
@Composable
private fun ErrorStatePreview() {
    NbaTheme {
        Surface { ErrorState(message = Res.string.error_network, onRetry = {}) }
    }
}
