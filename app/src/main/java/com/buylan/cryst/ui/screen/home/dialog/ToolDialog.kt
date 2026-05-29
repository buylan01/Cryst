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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.model.PanelPosition
import com.buylan.cryst.ui.screen.home.model.ToolAction
import com.buylan.cryst.vfs.ArchiveFile
import com.buylan.cryst.vfs.VirtualFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDialog(
    file: VirtualFile,
    position: PanelPosition,
    onDismiss: () -> Unit,
    onToolAction: (action: ToolAction) -> Unit
) {
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
                    @Composable
                    fun ToolItem(
                        text: String,
                        icon: Int,
                        onClick: () -> Unit,
                        enabled: Boolean = true
                    ) {
                        TextButton(
                            enabled = enabled,
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text)
                        }
                    }

                    ToolItem(
                        text = if (position == PanelPosition.R)
                            stringResource(R.string.copy_to_left)
                        else
                            stringResource(R.string.copy_to_right),
                        icon = R.drawable.ic_file_copy,
                        onClick = { onToolAction(ToolAction.Copy) }
                    )

                    ToolItem(
                        text = stringResource(R.string.delete),
                        icon = R.drawable.ic_delete,
                        onClick = { onToolAction(ToolAction.Delete) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.open_with),
                        enabled = !file.isDirectory,
                        icon = R.drawable.ic_open_with,
                        onClick = { onToolAction(ToolAction.OpenWith) }
                    )

                    ToolItem(
                        text = stringResource(R.string.info),
                        icon = R.drawable.ic_info,
                        onClick = {
                            onToolAction(ToolAction.Properties)
                        }
                    )

                    ToolItem(
                        text = if (position == PanelPosition.R)
                            stringResource(R.string.move_to_left)
                        else
                            stringResource(R.string.move_to_right),
                        icon = R.drawable.ic_content_cut,
                        onClick = { onToolAction(ToolAction.Move) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.rename),
                        icon = R.drawable.ic_edit,
                        onClick = { onToolAction(ToolAction.Rename) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.compress),
                        icon = R.drawable.ic_archive,
                        onClick = { onToolAction(ToolAction.Compress) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.share),
                        icon = R.drawable.ic_share,
                        onClick = { onToolAction(ToolAction.Share) },
                        enabled = !file.isDirectory
                    )
                }
            }
        }
    )
}