package com.prolan.catu.ui.screen.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.prolan.catu.R
import com.prolan.catu.ui.screen.home.model.FileOperaUiState
import com.prolan.catu.util.moveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MoveDialog(
    source: File,
    target: File,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val _uiState = MutableStateFlow<FileOperaUiState>(FileOperaUiState.Idle)
    val uiState by _uiState.collectAsState()
    AlertDialog(
        modifier = Modifier.width(560.dp),
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.move)) },
        text = {
            Column {
                when (uiState) {
                    is FileOperaUiState.Idle -> Text("是否移动 ${source.name} 到 $target ?")
                    is FileOperaUiState.InProgress -> LinearProgressIndicator()
                    is FileOperaUiState.Progress -> { }
                    is FileOperaUiState.Success -> Text(
                        "移动成功",
                        color = MaterialTheme.colorScheme.primary
                    )

                    is FileOperaUiState.Error -> Text(
                        (uiState as FileOperaUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        _uiState.emit(FileOperaUiState.InProgress)
                        val result = moveFile(source, File("$target/${source.name}"))
                        if (result) {
                            onDismiss()
                            onRefresh()
                        } else {
                            _uiState.emit(FileOperaUiState.Error("Unknown"))
                        }

                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
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