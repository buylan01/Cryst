package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.component.Segment
import com.buylan.cryst.util.createTar
import com.buylan.cryst.util.createZip
import com.buylan.cryst.util.invalidChars
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CompressDialog(
    source: List<VirtualFile>,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {

    var fileName by remember { mutableStateOf(
        (if (source.size == 1) source[0].name else source[0].parentFile?.name) + ".zip"
    ) }
    var createFail by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var archiveFormat by remember { mutableStateOf(ArchiveFormat.ZIP) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.compress)) },
        text = {
            val hasInvalidChar = remember(fileName) {
                fileName.any { it in invalidChars }
            }
            val isEmpty = fileName.isBlank()
            val isValid = !hasInvalidChar && !isEmpty && !createFail
            var showDarkModeMenu by remember { mutableStateOf(false) }

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
                                text = stringResource(R.string.filename_cannot_be_empty),
                                color = MaterialTheme.colorScheme.error
                            )

                            hasInvalidChar -> Text(
                                text = stringResource(R.string.filename_cannot_contain,invalidChars.joinToString("")),
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
                Segment(
                    modifier = Modifier
                        .clip(shape = MaterialTheme.shapes.small)
                        .background(color = MaterialTheme.colorScheme.surfaceContainer)
                        .height(54.dp),
                    onClick = { showDarkModeMenu = true },
                    title = {
                        Text(
                            stringResource(R.string.format),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    text = {
                        Text(
                            text = archiveFormat.name
                        )
                        DropdownMenu(
                            expanded = showDarkModeMenu,
                            onDismissRequest = { showDarkModeMenu = false }
                        ) {
                            ArchiveFormat.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.name) },
                                    onClick = {
                                        archiveFormat = it
                                        showDarkModeMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
                if (loading) LinearProgressIndicator()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            loading = true

                            val allFiles = source.flatMap { item ->
                                val ioFile = item.toFile()
                                if (item.isDirectory) {
                                    ioFile.walkTopDown().toList()
                                } else {
                                    listOf(ioFile)
                                }
                            }

                            val baseDir = if (source.isNotEmpty()) {
                                val parents = source.map { it.parentFile }.toSet()
                                if (parents.size == 1) parents.first()!!.toFile()
                                else source.first().parentFile!!.toFile()
                            } else {
                                error("No source files selected")
                            }

                            val outputPath = File(baseDir, fileName)

                            when (archiveFormat) {
                                ArchiveFormat.ZIP -> createZip(
                                    files = allFiles,
                                    outputPath,
                                    baseDir
                                )
                                ArchiveFormat.TAR -> createTar(
                                    files = allFiles,
                                    outputPath,
                                    baseDir
                                )
                            }
                            loading = false
                            onRefresh()
                            onDismiss()
                        }
                    }
                },
                enabled = !loading
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    scope.cancel()
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