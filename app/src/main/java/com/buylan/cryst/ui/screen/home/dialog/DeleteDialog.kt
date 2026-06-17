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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.model.DeleteFileViewModel
import com.buylan.cryst.ui.screen.home.model.FileOperaUiState
import com.buylan.cryst.vfs.VirtualFile
import java.io.File

@Composable
fun DeleteDialog(
    targetFiles: List<VirtualFile>,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val viewModel: DeleteFileViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is FileOperaUiState.Success && (uiState as FileOperaUiState.Success).all) {
            onDismiss()
            onRefresh()
            viewModel.finish()
        }
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.finish()
            onDismiss()
        },
        title = { Text(stringResource(R.string.delete)) },
        text = {
            Column {
                when (uiState) {
                    is FileOperaUiState.Idle -> {
                        Text(stringResource(R.string.confirm_delete, targetFiles.map { it.name }), modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            stringResource(R.string.delete_file_warning),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is FileOperaUiState.InProgress -> LinearProgressIndicator()
                    is FileOperaUiState.Progress -> {
                        val prog = uiState as FileOperaUiState.Progress
                        LinearProgressIndicator(
                            progress = { prog.percentage / 100f },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "进度: ${prog.percentage}%  ${prog.current}/${prog.total} (失败 ${prog.failed})",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    is FileOperaUiState.Success -> Text(
                        stringResource(R.string.delete_success),
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
            Button(
                onClick = { viewModel.startDelete(targetFiles.map { File(it.absolutePath) }) },
                enabled = uiState is FileOperaUiState.Idle || uiState is FileOperaUiState.Error
            ) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.finish()
                    onDismiss()
                }
            ) { Text(stringResource(R.string.cancel)) }
        }
    )
}