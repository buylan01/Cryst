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

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.buylan.cryst.R
import com.buylan.cryst.util.invalidChars
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile

@Composable
fun RenameDialog(
    file: VirtualFile,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val textFieldState = rememberTextFieldState(initialText = file.name)
    var renameFail by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.rename)) },
        text = {
            val currentText = textFieldState.text
            val hasInvalidChar = remember(currentText) {
                currentText.any { it in invalidChars }
            }
            val isEmpty = currentText.isBlank()
            val isValid = !hasInvalidChar && !isEmpty && !renameFail
            val focusRequester = remember { FocusRequester() }

            TextField(
                state = textFieldState,
                shape = MaterialTheme.shapes.small,
                isError = !isValid,
                supportingText = {
                    when {
                        isEmpty -> Text(
                            stringResource(R.string.filename_cannot_be_empty),
                            color = MaterialTheme.colorScheme.error
                        )

                        hasInvalidChar -> Text(
                            stringResource(
                                R.string.filename_cannot_contain,
                                invalidChars.joinToString("")
                            ),
                            color = MaterialTheme.colorScheme.error
                        )

                        renameFail -> Text(
                            stringResource(R.string.rename_failed),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.focusRequester(focusRequester)
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val renamer = file.renameTo(LocalFile("${file.parent}/${textFieldState.text}"))
                    if (!renamer) renameFail = true else {
                        onRefresh()
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}