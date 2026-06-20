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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.model.FileOperaUiState
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path

@Composable
fun MoveDialog(
    source: List<VirtualFile>,
    target: VirtualFile,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uiStateFlow = MutableStateFlow<FileOperaUiState>(FileOperaUiState.Idle)
    val uiState by uiStateFlow.collectAsState()
    AlertDialog(
        modifier = Modifier.width(560.dp),
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.move)) },
        text = {
            Column {
                when (uiState) {
                    is FileOperaUiState.Idle -> Text(
                        stringResource(
                            R.string.confirm_move_to,
                            source.map { it.name },
                            target.absolutePath
                        ))
                    is FileOperaUiState.InProgress -> LinearProgressIndicator()
                    is FileOperaUiState.Progress -> { }
                    is FileOperaUiState.Success -> Text(
                        stringResource(R.string.move_to_success),
                        color = MaterialTheme.colorScheme.primary
                    )

                    is FileOperaUiState.Error -> Text(
                        text = stringResource((uiState as FileOperaUiState.Error).messageResId),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        uiStateFlow.emit(FileOperaUiState.InProgress)
                        var allSuccess = true
                        for (file in source) {
                            val success = try {
                                Files.move(
                                    Path(file.absolutePath),
                                    Path("${target.absolutePath}/${file.name}")
                                )
                                true
                            } catch (_: IOException) {
                                false
                            }
                            if (!success) {
                                allSuccess = false
                            }
                        }
                        if (allSuccess) {
                            onDismiss()
                            onRefresh()
                        } else {
                            uiStateFlow.emit(FileOperaUiState.Error(R.string.move_to_failed))
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.move))
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