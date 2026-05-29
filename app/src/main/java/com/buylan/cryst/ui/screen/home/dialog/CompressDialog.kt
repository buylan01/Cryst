package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.buylan.cryst.R
import com.buylan.cryst.util.createTar
import com.buylan.cryst.util.createZip
import com.buylan.cryst.util.invalidChars
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import java.io.File

@Composable
fun CompressDialog(
    source: VirtualFile,
    onDismiss: () -> Unit,
    onRefresh: (VirtualFile) -> Unit
) {

    var fileName by remember { mutableStateOf(source.name + ".zip") }
    var createFail by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.compress)) },
        text = {
            val hasInvalidChar = remember(fileName) {
                fileName.any { it in invalidChars }
            }
            val isEmpty = fileName.isBlank()
            val isValid = !hasInvalidChar && !isEmpty && !createFail

            Column {
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
                                "文件名不能为空",
                                color = MaterialTheme.colorScheme.error
                            )

                            hasInvalidChar -> Text(
                                "不能包含: ${invalidChars.joinToString("")}",
                                color = MaterialTheme.colorScheme.error
                            )

                            createFail -> Text(
                                "创建失败",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val creator = createZip(files = listOf(source.toFile()), File(source.toFile().parent!! + File.separator + source.name + ".zip"))
                    //createTar(files = listOf(source.toFile()), File(source.toFile().parent!! + File.separator + source.name + ".tar"))
                    onRefresh(LocalFile(creator.absolutePath))
                    onDismiss()
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