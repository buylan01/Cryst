/*
 *    Copyright 2026 buylan
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.buylan.cryst.R
import com.buylan.cryst.util.PanelPosition
import com.buylan.cryst.util.ToolAction
import com.buylan.cryst.vfs.ArchiveFile
import com.buylan.cryst.vfs.VirtualFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDialog(
    files: List<VirtualFile>,
    position: PanelPosition,
    onDismiss: () -> Unit,
    onToolAction: (action: ToolAction) -> Unit
) {

    val toolItems = remember(files, position) {
        val isArchive = files.singleOrNull() is ArchiveFile
        val isNotDirectory = files.singleOrNull()?.isDirectory == false
        val isSingleFile = files.size == 1

        listOf(
            ToolItemConfig(
                text = if (position == PanelPosition.R) R.string.copy_to_left else R.string.copy_to_right,
                icon = R.drawable.ic_file_copy,
                onClick = { onToolAction(ToolAction.Copy) }
            ),
            ToolItemConfig(
                text = R.string.delete,
                icon = R.drawable.ic_delete,
                onClick = { onToolAction(ToolAction.Delete) },
                enabled = !isArchive
            ),
            ToolItemConfig(
                text = R.string.open_with,
                icon = R.drawable.ic_open_with,
                onClick = { onToolAction(ToolAction.OpenWith) },
                enabled = isNotDirectory
            ),
            ToolItemConfig(
                text = R.string.info,
                icon = R.drawable.ic_info,
                onClick = { onToolAction(ToolAction.Properties) }
            ),
            ToolItemConfig(
                text = if (position == PanelPosition.R) R.string.move_to_left else R.string.move_to_right,
                icon = R.drawable.ic_content_cut,
                onClick = { onToolAction(ToolAction.Move) },
                enabled = !isArchive
            ),
            ToolItemConfig(
                text = R.string.rename,
                icon = R.drawable.ic_edit,
                onClick = { onToolAction(ToolAction.Rename) },
                enabled = !isArchive && isSingleFile
            ),
            ToolItemConfig(
                text = R.string.compress,
                icon = R.drawable.ic_archive,
                onClick = { onToolAction(ToolAction.Compress) },
                enabled = !isArchive
            ),
            ToolItemConfig(
                text = R.string.share,
                icon = R.drawable.ic_share,
                onClick = { onToolAction(ToolAction.Share) },
                enabled = isNotDirectory
            )
        )
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        content = {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                FlowColumn(
                    modifier = Modifier
                        .padding(12.dp),
                    maxItemsInEachColumn = 4
                ) {

                    toolItems.fastForEach { action ->
                        TextButton(
                            enabled = action.enabled,
                            onClick = action.onClick,
                            colors = ButtonDefaults.buttonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Icon(
                                painter = painterResource(action.icon),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(action.text))
                        }
                    }
                }
            }
        }
    )
}

private data class ToolItemConfig(
    val text: Int,
    val icon: Int,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)