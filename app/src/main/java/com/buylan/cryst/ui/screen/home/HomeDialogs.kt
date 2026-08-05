package com.buylan.cryst.ui.screen.home

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.buylan.cryst.BuildConfig
import com.buylan.cryst.R
import com.buylan.cryst.ui.Screen
import com.buylan.cryst.ui.screen.home.dialog.ApkDialog
import com.buylan.cryst.ui.screen.home.dialog.AudioPlayer
import com.buylan.cryst.ui.screen.home.dialog.CompressDialog
import com.buylan.cryst.ui.screen.home.dialog.CopyDialog
import com.buylan.cryst.ui.screen.home.dialog.CreateDialog
import com.buylan.cryst.ui.screen.home.dialog.DeleteDialog
import com.buylan.cryst.ui.screen.home.dialog.MoveDialog
import com.buylan.cryst.ui.screen.home.dialog.OpenWithDialog
import com.buylan.cryst.ui.screen.home.dialog.PropertiesDialog
import com.buylan.cryst.ui.screen.home.dialog.RenameDialog
import com.buylan.cryst.ui.screen.home.dialog.ScriptDialog
import com.buylan.cryst.ui.screen.home.dialog.SearchDialog
import com.buylan.cryst.ui.screen.home.dialog.SortOrderDialog
import com.buylan.cryst.ui.screen.home.dialog.ToolDialog
import com.buylan.cryst.ui.screen.home.model.DialogsEvent
import com.buylan.cryst.ui.screen.home.model.DialogsViewModel
import com.buylan.cryst.ui.screen.home.model.HomeUiState
import com.buylan.cryst.ui.screen.home.model.HomeViewModel
import com.buylan.cryst.ui.screen.home.model.PanelStates
import com.buylan.cryst.util.FileType
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeDialogs(
    viewModel: DialogsViewModel,
    homeViewModel: HomeViewModel,
    context: Context,
    currentPanel: PanelStates,
    uiState: HomeUiState,
    handleRefresh: (VirtualFile?) -> Unit,
    onNavigate: (Screen) -> Unit,
) {

    val event by viewModel.currentEvent.collectAsState()

    event?.let { dialog ->
        when (dialog) {
            DialogsEvent.AboutDialog -> {
                BasicAlertDialog(
                    onDismissRequest = viewModel::dismiss,
                    content = {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.width(280.dp)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(color = MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_folder),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = stringResource(R.string.app_name),
                                            style = MaterialTheme.typography.bodyLarge,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 160.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = BuildConfig.VERSION_NAME,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        onNavigate(Screen.Licenses)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.open_source_libraries))
                                }
                            }
                        }
                    }
                )
            }

            DialogsEvent.PathDialog -> {
                val textFieldState =
                    rememberTextFieldState(initialText = currentPanel.path.absolutePath)
                AlertDialog(
                    onDismissRequest = viewModel::dismiss,
                    title = { Text(stringResource(R.string.go_to_path)) },
                    text = {
                        val focusRequester = remember { FocusRequester() }

                        OutlinedTextField(
                            state = textFieldState,
                            label = { Text(stringResource(R.string.path)) },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.focusRequester(focusRequester)
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                homeViewModel.currentPanelViewModel.setPath(LocalFile(textFieldState.text.toString()))
                                viewModel.dismiss()
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = viewModel::dismiss
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            DialogsEvent.PermissionRequest -> {
                AlertDialog(
                    onDismissRequest = viewModel::dismiss,
                    title = { Text(stringResource(R.string.permission_request)) },
                    text = {
                        Text(stringResource(R.string.app_name) + stringResource(R.string.permission_manage_file_require))
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intent =
                                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                context.startActivity(intent)
                                viewModel.dismiss()
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                Toast.makeText(context, "TAT", Toast.LENGTH_SHORT).show()
                                viewModel.dismiss()
                            }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            DialogsEvent.SortDialog -> {
                SortOrderDialog(
                    onDismiss = viewModel::dismiss,
                    onSelect = { homeViewModel.currentPanelViewModel.setSort(it) },
                    currentPanel,
                    uiState.panelPosition
                )
            }

            is DialogsEvent.ApkDialog -> {
                ApkDialog(
                    targetFile = dialog.file as LocalFile,
                    onDismiss = viewModel::dismiss,
                    unpack = {
                        homeViewModel.handleFileClick(
                            context = context,
                            onNavigate = onNavigate,
                            file = dialog.file,
                            type = FileType.ARCHIVE
                        )
                    }
                )
            }

            is DialogsEvent.AudioDialog -> {
                AudioPlayer(onDismiss = viewModel::dismiss, dialog.file)
            }

            is DialogsEvent.CompressDialog -> {
                CompressDialog(dialog.files, onDismiss = viewModel::dismiss) {
                    handleRefresh(dialog.files.first().parentFile)
                }
            }

            is DialogsEvent.CopyDialog -> {
                CopyDialog(
                    source = dialog.state.files,
                    target = dialog.state.path,
                    onDismiss = viewModel::dismiss,
                    onRefresh = {
                        handleRefresh(dialog.state.path)
                    }
                )
            }

            is DialogsEvent.CreateDialog -> {
                CreateDialog(onDismiss = viewModel::dismiss, dialog.parent) {
                    handleRefresh(dialog.parent)
                }
            }

            is DialogsEvent.DeleteDialog -> {
                DeleteDialog(
                    targetFiles = dialog.files,
                    onDismiss = viewModel::dismiss,
                    onRefresh = {
                        handleRefresh(dialog.files.first().parentFile)
                    }
                )
            }

            is DialogsEvent.MoveDialog -> {
                val state = dialog.state
                MoveDialog(
                    source = state.files,
                    target = state.path,
                    onDismiss = viewModel::dismiss,
                    onRefresh = {
                        handleRefresh(state.files.first().parentFile)
                        handleRefresh(state.path)
                    }
                )
            }

            is DialogsEvent.OpenWithDialog -> {
                OpenWithDialog(
                    onDismiss = viewModel::dismiss
                ) {
                    homeViewModel.handleFileClick(
                        context = context,
                        onNavigate = onNavigate,
                        file = dialog.file,
                        type = it
                    )
                }
            }

            is DialogsEvent.PropertiesDialog -> {
                PropertiesDialog(dialog.files, viewModel::dismiss)
            }

            is DialogsEvent.RenameDialog -> {
                RenameDialog(dialog.file, viewModel::dismiss) {
                    handleRefresh(dialog.file.parentFile)
                }
            }

            is DialogsEvent.RunScriptDialog -> {
                ScriptDialog(viewModel::dismiss, dialog.file)
            }

            is DialogsEvent.SearchDialog -> {
                SearchDialog(onDismiss = viewModel::dismiss, dialog.root) { file ->
                    homeViewModel.currentPanelViewModel.setPath(file)
                    viewModel.dismiss()
                }
            }

            is DialogsEvent.ToolsDialog -> {
                ToolDialog(
                    dialog.files, uiState.panelPosition,
                    onDismiss = viewModel::dismiss,
                    onToolAction = { homeViewModel.onToolAction(context, it, dialog.files) },
                )
            }
        }
    }
}