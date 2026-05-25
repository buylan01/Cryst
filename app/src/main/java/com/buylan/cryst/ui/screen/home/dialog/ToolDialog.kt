package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.model.PanelPosition
import com.buylan.cryst.ui.screen.home.model.ToolAction
import com.buylan.cryst.util.shareFile
import com.buylan.cryst.vfs.ArchiveFile
import com.buylan.cryst.vfs.VirtualFile
import java.io.File

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

                    val context = LocalContext.current

                    @Composable
                    fun ToolItem(
                        text: String,
                        icon: ImageVector,
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
                                imageVector = icon,
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
                        icon = Icons.Default.FileCopy,
                        onClick = { onToolAction(ToolAction.Copy) }
                    )

                    ToolItem(
                        text = stringResource(R.string.delete),
                        icon = Icons.Default.Delete,
                        onClick = { onToolAction(ToolAction.Delete) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.open_with),
                        enabled = !file.isDirectory,
                        icon = Icons.Default.FileOpen,
                        onClick = { onToolAction(ToolAction.OpenWith) }
                    )

                    ToolItem(
                        text = stringResource(R.string.info),
                        icon = Icons.Default.Info,
                        onClick = {
                            onToolAction(ToolAction.Properties)
                        }
                    )

                    ToolItem(
                        text = if (position == PanelPosition.R)
                            stringResource(R.string.move_to_left)
                        else
                            stringResource(R.string.move_to_right),
                        icon = Icons.AutoMirrored.Filled.DriveFileMove,
                        onClick = { onToolAction(ToolAction.Move) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.rename),
                        icon = Icons.Default.DriveFileRenameOutline,
                        onClick = { onToolAction(ToolAction.Rename) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.compress),
                        icon = Icons.Default.Archive,
                        onClick = { onToolAction(ToolAction.Compress) },
                        enabled = file !is ArchiveFile
                    )

                    ToolItem(
                        text = stringResource(R.string.share),
                        icon = Icons.Default.Share,
                        onClick = { onToolAction(ToolAction.Share) },
                        enabled = !file.isDirectory
                    )
                }
            }
        }
    )
}