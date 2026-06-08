package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.FileRow
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    targetPath: VirtualFile,
    onFileClick: (VirtualFile) -> Unit
) {
    var searchFileName by remember { mutableStateOf("") }
    val found = remember { mutableStateListOf<VirtualFile>() }
    var tonalCount by remember { mutableIntStateOf(0) }
    var foundCount by remember { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var includeSub by remember { mutableStateOf(true) }

    LaunchedEffect(isSearching) {
        withContext(Dispatchers.IO) {
            if (isSearching) {
                val candidates = if (includeSub) {
                    targetPath.walkTopDownSequence()
                } else {
                    targetPath.listFiles()?.asSequence() ?: emptySequence()
                }

                candidates.forEach { file ->
                    tonalCount++
                    if (file.name.contains(searchFileName, true)) {
                        found.add(file)
                        foundCount++
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

                FilterChip(
                    selected = includeSub,
                    onClick = { includeSub = !includeSub },
                    label = { Text(stringResource(R.string.search_subdirectories)) },
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }

                if (isSearching || found.isNotEmpty()) {
                    Text("在" + "$tonalCount" + "个文件里找到" + "$foundCount")
                }

                if (found.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(found) { file ->
                            FileRow(
                                file = file,
                                type = getFileType(file),
                                onClick = {
                                    onFileClick(file)
                                },
                                onLongClick = {
                                    //onFileClick(file, null)
                                },
                                onSwipe = {}
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    foundCount = 0
                    tonalCount = 0
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