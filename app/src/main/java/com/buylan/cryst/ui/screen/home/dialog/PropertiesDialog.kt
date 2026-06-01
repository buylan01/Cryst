package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.util.formatFileDate
import com.buylan.cryst.util.getFileSize
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.vfs.VirtualFile

@Composable
fun PropertiesDialog(
    files: List<VirtualFile>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.info))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                @Composable
                fun PropertyRow(
                    label: Int,
                    value: String
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                PropertyRow(label = R.string.name, value = files.singleOrNull()?.name ?: files.map { it.name }.toString())
                PropertyRow(label = R.string.path, value = files[0].parent ?: stringResource(R.string.unknown))
                if (files.size == 1) {
                    PropertyRow(label = R.string.type, value = stringResource(getFileType(files[0]).label))
                    PropertyRow(label = R.string.size, value = getFileSize(files[0]))
                    PropertyRow(label = R.string.time, value = formatFileDate(files[0]))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }
    )
}