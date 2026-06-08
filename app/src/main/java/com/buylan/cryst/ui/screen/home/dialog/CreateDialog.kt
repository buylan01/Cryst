package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.buylan.cryst.R
import com.buylan.cryst.util.createFile
import com.buylan.cryst.util.createFolder
import com.buylan.cryst.util.invalidChars
import com.buylan.cryst.vfs.VirtualFile

@Composable
fun CreateDialog(
    onDismiss: () -> Unit,
    targetPath: VirtualFile,
    onRefresh: () -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var createFail by remember { mutableStateOf(false) }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf(stringResource(R.string.file), stringResource(R.string.folder))

    fun createFile(isFolder: Boolean, name: String) {
        val creator = if (isFolder) createFolder(targetPath, name) else createFile(targetPath, name)
        if (!creator) createFail = true else {
            onDismiss()
            onRefresh()
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.create)) },
        text = {
            val hasInvalidChar = remember(fileName) {
                fileName.any { it in invalidChars }
            }
            val isEmpty = fileName.isBlank()
            val isValid = !hasInvalidChar && !isEmpty && !createFail
            val focusRequester = remember { FocusRequester() }

            Column() {
                TextField(
                    value = fileName,
                    onValueChange = {
                        fileName = it
                        createFail = false
                    },
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
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = { selectedIndex = index },
                            selected = index == selectedIndex,
                            label = { Text(label) }
                        )
                    }
                }
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    createFile(selectedIndex == 1, fileName)
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