package com.nb.app.core.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nb.app.core.ui.theme.NbaTheme
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.ic_chevron_right
import nbapp.shared.generated.resources.open_details_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val ChevronSize = 20.dp
private val HorizontalPadding = 16.dp

/** Describes one column of a [DataTable]. [weight] sets its share of the row width. */
@Immutable
data class TableColumn(
    val title: String,
    val weight: Float = 1f,
    val textAlign: TextAlign = TextAlign.Start,
)

/**
 * A simple table: a sticky header with the column titles and one row per item.
 *
 * Rows are clickable (and show a chevron) when [onItemClick] is set. [footer] adds extra items
 * after the rows, e.g. a "loading more" indicator for endless scroll.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> DataTable(
    columns: List<TableColumn>,
    items: List<T>,
    key: (T) -> Any,
    cells: (T) -> List<String>,
    modifier: Modifier = Modifier,
    onItemClick: ((T) -> Unit)? = null,
    state: LazyListState = rememberLazyListState(),
    footer: LazyListScope.() -> Unit = {},
) {
    val isClickable = onItemClick != null
    LazyColumn(modifier = modifier, state = state) {
        stickyHeader(key = "table_header") {
            TableHeader(columns = columns, reserveChevronSpace = isClickable)
        }
        items(items = items, key = key) { item ->
            TableRow(
                columns = columns,
                cells = cells(item),
                onClick = onItemClick?.let { { it(item) } },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = HorizontalPadding),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
        footer()
    }
}

@Composable
fun TableHeader(
    columns: List<TableColumn>,
    modifier: Modifier = Modifier,
    reserveChevronSpace: Boolean = false,
) {
    // Opaque so rows scrolling under the sticky header stay hidden.
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = HorizontalPadding, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                columns.forEach { column ->
                    TableCell(
                        text = column.title,
                        column = column,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (reserveChevronSpace) Spacer(Modifier.size(ChevronSize))
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = HorizontalPadding))
        }
    }
}

@Composable
fun TableRow(
    columns: List<TableColumn>,
    cells: List<String>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    require(cells.size == columns.size) { "Expected ${columns.size} cells but got ${cells.size}" }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = HorizontalPadding, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.zip(cells).forEach { (column, text) ->
            TableCell(text = text, column = column, style = MaterialTheme.typography.bodyMedium)
        }
        if (onClick != null) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right),
                contentDescription = stringResource(Res.string.open_details_description),
                modifier = Modifier.size(ChevronSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    column: TableColumn,
    style: TextStyle,
    fontWeight: FontWeight? = null,
) {
    Text(
        text = text,
        modifier = Modifier.weight(column.weight),
        style = style,
        fontWeight = fontWeight,
        textAlign = column.textAlign,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Preview
@Composable
private fun DataTablePreview() {
    NbaTheme {
        Surface {
            DataTable(
                columns = listOf(
                    TableColumn("Name", weight = 1.4f),
                    TableColumn("City"),
                    TableColumn("Conference", textAlign = TextAlign.End),
                ),
                items = listOf(
                    listOf("Atlanta Hawks", "Atlanta", "East"),
                    listOf("Boston Celtics", "Boston", "East"),
                    listOf("Golden State Warriors", "Golden State", "West"),
                ),
                key = { it.first() },
                cells = { it },
                onItemClick = {},
            )
        }
    }
}
