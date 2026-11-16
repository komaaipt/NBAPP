package com.nb.app.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import nbapp.shared.generated.resources.Res
import nbapp.shared.generated.resources.cancel
import nbapp.shared.generated.resources.sort_dialog_title
import org.jetbrains.compose.resources.stringResource

/** Lets the user pick how the team list is ordered. Picking an option applies it right away. */
@Composable
fun SortDialog(
    selectedOption: TeamSortOption,
    onOptionSelected: (TeamSortOption) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.sort_dialog_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                TeamSortOption.entries.forEach { option ->
                    SortOptionRow(
                        label = stringResource(option.label),
                        selected = option == selectedOption,
                        onClick = { onOptionSelected(option) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )
}

@Composable
private fun SortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The whole row handles the click, so the radio button itself doesn't need one.
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
