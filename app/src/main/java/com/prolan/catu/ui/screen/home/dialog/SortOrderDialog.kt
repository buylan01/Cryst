package com.prolan.catu.ui.screen.home.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.prolan.catu.R
import com.prolan.catu.ui.screen.home.PanelStates
import com.prolan.catu.ui.screen.home.model.PanelPosition
import com.prolan.catu.ui.screen.home.model.SortType

@Composable
fun SortOrderDialog(
    onDismiss: () -> Unit,
    panelStates: PanelStates,
    currentPanel: PanelPosition
) {
    var selectedSortOption by remember { mutableStateOf(SortType.NAME) }
    selectedSortOption = panelStates.sortType

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(if (currentPanel == PanelPosition.L) R.string.sort_left else R.string.sort_right)) },
        text = {
            Column(
                modifier = Modifier
            ) {
                SortType.entries.forEach { sortType ->
                    ListItem(
                        headlineContent = { Text(stringResource(sortType.label)) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedSortOption == sortType,
                                onClick = { selectedSortOption = sortType }
                            )
                        },
                        modifier = Modifier
                            .clickable { selectedSortOption = sortType },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    panelStates.sortType = selectedSortOption
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }
    )
}