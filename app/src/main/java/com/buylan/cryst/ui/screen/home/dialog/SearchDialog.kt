package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.FileRow
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.vfs.LocalFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    targetPath: Path,
    onFileClick: (File) -> Unit
) {
    var searchFileName by remember { mutableStateOf("") }
    val found = remember { mutableStateListOf<File>() }
    var processedFiles by remember { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var includeSub by remember { mutableStateOf(true) }

    LaunchedEffect(isSearching) {
        withContext(Dispatchers.IO) {
            if (isSearching) {
                if (includeSub) {
                    Files.walkFileTree(targetPath, object : SimpleFileVisitor<Path>() {
                        override fun preVisitDirectory(
                            dir: Path,
                            attrs: BasicFileAttributes
                        ): FileVisitResult {
                            return try {
                                FileVisitResult.CONTINUE
                            } catch (_: AccessDeniedException) {
                                FileVisitResult.SKIP_SUBTREE
                            } catch (_: SecurityException) {
                                FileVisitResult.SKIP_SUBTREE
                            }
                        }

                        override fun visitFile(
                            file: Path,
                            attrs: BasicFileAttributes
                        ): FileVisitResult {
                            processedFiles++
                            if (file.fileName.toString().contains(searchFileName, true)) {
                                found.add(file.toFile())
                            }
                            return FileVisitResult.CONTINUE
                        }

                        override fun visitFileFailed(
                            file: Path,
                            exc: IOException
                        ): FileVisitResult {
                            return FileVisitResult.SKIP_SUBTREE
                        }
                    })
                } else {
                    Files.list(targetPath).use { stream ->
                        stream.forEach { path ->
                            processedFiles++
                            if (path.fileName.toString().contains(searchFileName, true)) {
                                found.add(path.toFile())
                            }
                        }
                    }
                }
                isSearching = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.search)) },
        text = {
            Column {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    value = searchFileName,
                    onValueChange = { searchFileName = it },
                    modifier = Modifier.focusRequester(focusRequester),
                    shape = MaterialTheme.shapes.small,
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .offset(x = (-12).dp)
                ) {
                    Checkbox(
                        checked = includeSub,
                        onCheckedChange = { includeSub = it }
                    )
                    Text(stringResource(R.string.search_subdirectories))
                }

                if (isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                    Text("$processedFiles")
                }

                if (found.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(found) { file ->
                            FileRow(
                                file = LocalFile(file),
                                type = getFileType(LocalFile(file)),
                                onFileClick = {
                                    onFileClick(file)
                                },
                                onFileLongClick = {
                                    //onFileClick(file, null)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    processedFiles = 0
                    found.clear()
                    isSearching = true
                },
                enabled = !isSearching && searchFileName.isNotEmpty()
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                enabled = !isSearching
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}