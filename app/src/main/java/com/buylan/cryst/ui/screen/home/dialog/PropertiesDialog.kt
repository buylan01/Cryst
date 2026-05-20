package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.util.formatFileDate
import com.buylan.cryst.util.getFileSize
import com.buylan.cryst.util.getFileType
import java.io.File

@Composable
fun PropertiesDialog(
    file: File,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.info))
        },
        text = {
            val fileSize = remember(file) { getFileSize(file) }
            val formattedDate = remember(file) { formatFileDate(file) }
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

                PropertyRow(label = R.string.name, value = file.name)
                PropertyRow(label = R.string.path, value = file.parent ?: "无")
                PropertyRow(label = R.string.type, value = stringResource(getFileType(file).label))
                PropertyRow(label = R.string.size, value = fileSize)
                PropertyRow(label = R.string.time, value = formattedDate)
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