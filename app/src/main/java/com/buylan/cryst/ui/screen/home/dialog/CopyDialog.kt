package com.buylan.cryst.ui.screen.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.buylan.cryst.ui.screen.home.model.CopyFileViewModel
import com.buylan.cryst.ui.screen.home.model.FileOperaUiState
import com.buylan.cryst.vfs.VirtualFile

@Composable
fun CopyDialog(
    source: List<VirtualFile>,
    target: VirtualFile,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val viewModel: CopyFileViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is FileOperaUiState.Success && (uiState as FileOperaUiState.Success).all) {
            onDismiss()
            onRefresh()
            viewModel.finish()
        }
    }

    AlertDialog(
        modifier = Modifier.width(560.dp),
        onDismissRequest = {
            onDismiss()
            viewModel.finish()
        },
        title = { Text(stringResource(R.string.copy)) },
        text = {
            Column {
                when (uiState) {
                    is FileOperaUiState.Idle -> Text(
                        stringResource(
                            R.string.confirm_copy_to,
                            source.map { it.name },
                            target.absolutePath
                        )
                    )
                    is FileOperaUiState.InProgress -> LinearProgressIndicator()
                    is FileOperaUiState.Progress -> {
                        val prog = uiState as FileOperaUiState.Progress
                        LinearProgressIndicator(
                            progress = { prog.percentage / 100f },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "${prog.percentage}%  ${prog.current}/${prog.total} (失败: ${prog.failed})",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    is FileOperaUiState.Success -> {
                        val isAll = (uiState as FileOperaUiState.Success).all
                        Text(
                            if(isAll) stringResource(R.string.copy_to_success) else stringResource(R.string.copy_to_failed),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    is FileOperaUiState.Error -> Text(
                        text = stringResource((uiState as FileOperaUiState.Error).messageResId),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.startCopy(source, target) },
                enabled = uiState is FileOperaUiState.Idle || uiState is FileOperaUiState.Error
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.finish()
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}