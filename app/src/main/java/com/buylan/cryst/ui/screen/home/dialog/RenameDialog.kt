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
                            "命名不能为空",
                            color = MaterialTheme.colorScheme.error
                        )

                        hasInvalidChar -> Text(
                            "不能包含: ${invalidChars.joinToString("")}",
                            color = MaterialTheme.colorScheme.error
                        )

                        renameFail -> Text(
                            "命名失败",
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