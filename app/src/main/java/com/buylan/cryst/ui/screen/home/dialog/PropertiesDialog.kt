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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.model.PropertiesViewModel
import com.buylan.cryst.util.formatFileDate
import com.buylan.cryst.util.formatSizeDetail
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.vfs.VirtualFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesDialog(
    files: List<VirtualFile>,
    onDismiss: () -> Unit
) {

    val viewModel: PropertiesViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val shouldComputeSize = files.size > 1 || files.first().isDirectory
    val showTime = files.size == 1
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(files) {
        if (shouldComputeSize) {
            viewModel.compute(files)
        }
    }

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
                    value: String,
                    onClick: (() -> Unit)? = null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = onClick != null,
                                onClick = onClick ?: {}
                            ),
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

                PropertyRow(
                    label = R.string.name,
                    value = files.singleOrNull()?.name ?: files.map { it.name }.toString()
                )
                PropertyRow(
                    label = R.string.path,
                    value = files.first().parent ?: stringResource(R.string.unknown)
                )
                if (showTime) {
                    PropertyRow(
                        label = R.string.time,
                        value = formatFileDate(files.first()),
                        onClick = { showDatePicker = true }
                    )
                }
                if (!shouldComputeSize) {
                    PropertyRow(
                        label = R.string.type,
                        value = stringResource(getFileType(files.first()).label)
                    )
                    PropertyRow(
                        label = R.string.size,
                        value = formatSizeDetail(files.first().length())
                    )
                } else {
                    PropertyRow(label = R.string.size, value = formatSizeDetail(uiState.totalSize))
                    PropertyRow(label = R.string.count, value = uiState.totalCount.toString())
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