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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.model.CompressViewModel
import com.buylan.cryst.ui.screen.home.model.FileOperaUiState
import com.buylan.cryst.util.invalidChars
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressDialog(
    source: List<VirtualFile>,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {

    val viewModel: CompressViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var createFail by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var archiveFormat by remember { mutableStateOf(ArchiveFormat.ZIP) }
    var level by rememberSaveable { mutableIntStateOf(6) }
    val textFieldState = rememberTextFieldState(archiveFormat.name)
    val levelFieldState = rememberTextFieldState(level.toString())
    var fileName by remember { mutableStateOf(
        (if (source.size == 1) source[0].name else source[0].parentFile?.name) + "." + archiveFormat.name.lowercase()
    ) }


    LaunchedEffect(uiState) {
        if (uiState is FileOperaUiState.Success && (uiState as FileOperaUiState.Success).all) {
            onDismiss()
            onRefresh()
            viewModel.finish()
        }
    }

    LaunchedEffect(archiveFormat) {
        val currentName = fileName
        val newExt = archiveFormat.name.lowercase()
        val knownExtensions = ArchiveFormat.entries.map { it.name.lowercase() }

        val lastDotIndex = currentName.lastIndexOf('.')
        fileName = if (lastDotIndex >= 0) {
            val possibleExt = currentName.substring(lastDotIndex + 1).lowercase()
            if (possibleExt in knownExtensions) {
                currentName.substring(0, lastDotIndex + 1) + newExt
            } else {
                currentName
            }
        } else {
            "$currentName.$newExt"
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.compress)) },
        text = {
            val hasInvalidChar = remember(fileName) {
                fileName.any { it in invalidChars }
            }
            val isEmpty = fileName.isBlank()
            val isValid = !hasInvalidChar && !isEmpty && !createFail
            var showFormatMenu by remember { mutableStateOf(false) }
            var showLevelMenu by remember { mutableStateOf(false) }

            Column {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = {
                        fileName = it
                        createFail = false
                    },
                    label = { Text(stringResource(R.string.name)) },
                    shape = MaterialTheme.shapes.small,
                    isError = !isValid,
                    supportingText = {
                        when {
                            isEmpty -> Text(
                                text = stringResource(R.string.filename_cannot_be_empty),
                                color = MaterialTheme.colorScheme.error
                            )

                            hasInvalidChar -> Text(
                                text = stringResource(
                                    R.string.filename_cannot_contain,
                                    invalidChars.joinToString("")
                                ),
                                color = MaterialTheme.colorScheme.error
                            )

                            createFail -> Text(
                                stringResource(R.string.create_failed),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = showFormatMenu,
                    onExpandedChange = { showFormatMenu = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        state = textFieldState,
                        shape = MaterialTheme.shapes.small,
                        readOnly = true,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        label = { Text(stringResource(R.string.format)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFormatMenu) },
                    )
                    ExposedDropdownMenu(
                        expanded = showFormatMenu,
                        onDismissRequest = { showFormatMenu = false },
                        matchAnchorWidth = false
                    ) {
                        ArchiveFormat.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        option.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    textFieldState.setTextAndPlaceCursorAtEnd(option.name)
                                    archiveFormat = option
                                    showFormatMenu = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = showLevelMenu,
                    onExpandedChange = { showLevelMenu = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        state = levelFieldState,
                        shape = MaterialTheme.shapes.small,
                        readOnly = true,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        label = { Text(stringResource(R.string.level)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLevelMenu) },
                    )
                    ExposedDropdownMenu(
                        expanded = showLevelMenu,
                        onDismissRequest = { showLevelMenu = false },
                        matchAnchorWidth = false
                    ) {
                        repeat(9) { index ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        index.toString(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    levelFieldState.setTextAndPlaceCursorAtEnd(index.toString())
                                    level = index
                                    showLevelMenu = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                when (uiState) {
                    is FileOperaUiState.InProgress -> LinearProgressIndicator()
                    is FileOperaUiState.Progress -> {
                        val prog = uiState as FileOperaUiState.Progress
                        LinearProgressIndicator(
                            progress = { prog.percentage / 100f },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "${prog.percentage}%  ${prog.current}/${prog.total} (失败: ${prog.failed})",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    is FileOperaUiState.Success -> {
                        val isAll = (uiState as FileOperaUiState.Success).all
                        Text(
                            if (isAll) stringResource(R.string.compress_success) else stringResource(
                                R.string.compress_failed
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    is FileOperaUiState.Error -> Text(
                        text = stringResource((uiState as FileOperaUiState.Error).messageResId),
                        color = MaterialTheme.colorScheme.error
                    )

                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.startCompress(archiveFormat, source, LocalFile(source[0].parent + "/" + fileName), level)
                },
                enabled = !loading
            ) {
                Text(stringResource(R.string.compress))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

enum class ArchiveFormat {
    ZIP,
    TAR
}